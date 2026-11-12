package com.liamryan.standandhold.common.npc;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;

/**
 * M33 D5 experiment: keyframed Army Rifleman motion through GeckoLib. Clips live in
 * {@code geckolib/animations/entity/army_rifleman.animation.json}. Every input is state the client already has (movement,
 * the synced aim flag, the launcher-shot entity event, hurt and death), so nothing new is saved or sent.
 */
public final class RiflemanAnimation {
    public static final String MOVEMENT = "movement", ACTION = "action", FIRE = "fire";
    public static final java.util.List<String> CLIPS = java.util.List.of("idle", "walk", "aim", "fire", "hurt", "death");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle"), WALK = RawAnimation.begin().thenLoop("walk"),
            AIM = RawAnimation.begin().thenLoop("aim"), SHOT = RawAnimation.begin().thenPlay("fire"),
            HURT = RawAnimation.begin().thenPlay("hurt"), DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private RiflemanAnimation() { }

    /** Legs and carry pose first; the action controller runs second, so aim, shots, hurt and death override the arms. */
    static void register(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<HumanNpc>(MOVEMENT, 4, test -> test.setAndContinue(test.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<HumanNpc>(ACTION, 3, RiflemanAnimation::action).triggerableAnim(FIRE, SHOT));
    }
    private static PlayState action(AnimationTest<HumanNpc> test) {
        HumanNpc npc = test.animatable();
        if (npc.isDeadOrDying()) { return test.setAndContinue(DEATH); }
        if (npc.hurtTime > 0) { return test.setAndContinue(HURT); }
        if (npc.launcherAim(1) > 0.5F) { return test.setAndContinue(AIM); }
        return PlayState.STOP;
    }
}
