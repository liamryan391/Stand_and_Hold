package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.sound.StandAndHoldSounds;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

/**
 * M33 presentation: the original sound events are registered and the server plays them where gameplay used vanilla stand-ins.
 * M35: every Army Rifleman arrives armed and fires the service rifle.
 */
final class PresentationTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    private PresentationTests() { }

    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("presentation_sounds_registered", PresentationTests::registered);
        tests.put("presentation_launcher_sound", PresentationTests::launcher);
        tests.put("presentation_rifleman_rifle", PresentationTests::rifle);
        return tests;
    }

    /** Server-side sounds heard during a test, recorded only while a test has armed the recorder. */
    private record Heard(SoundEvent sound, Vec3 at) { }
    private static final List<Heard> HEARD = new ArrayList<>();
    private static volatile boolean recording;
    static void hear(PlayLevelSoundEvent.AtPosition event) {
        if (recording && !event.getLevel().isClientSide() && event.getSound() != null) {
            synchronized (HEARD) { HEARD.add(new Heard(event.getSound().value(), event.getPosition())); }
        }
    }

    private static void registered(GameTestHelper h) {
        var expected = Map.of("npc.gear", StandAndHoldSounds.NPC_GEAR.get(), "launcher.fire", StandAndHoldSounds.LAUNCHER_FIRE.get(),
                "post.radio", StandAndHoldSounds.POST_RADIO.get(), "post.radio_urgent", StandAndHoldSounds.POST_RADIO_URGENT.get(),
                "rifle.fire", StandAndHoldSounds.RIFLE_FIRE.get());
        expected.forEach((path, sound) -> {
            var id = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, path);
            h.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(id) && BuiltInRegistries.SOUND_EVENT.getValue(id) == sound, "registered: " + id);
            h.assertTrue(sound.location().equals(id), "event names its own sounds.json entry: " + id);
        });
        h.succeed();
    }

    /**
     * M35: a Rifleman arrives with the service rifle, holds fire while a player is in the line of fire, then hits a zombie
     * at range with the rifle report (not melee). It keeps the rifle while the launcher is locked and swaps once it unlocks.
     */
    private static void rifle(GameTestHelper h) {
        QaWorld.begin(h);
        var npc = h.spawn(NpcRegistry.ARMY.get(), HOME);
        h.assertTrue(npc.getMainHandItem().is(EquipmentRegistry.SERVICE_RIFLE.get()), "a Rifleman arrives holding the service rifle");
        npc.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0); // isolate the rifle from melee
        var zombie = h.spawn(EntityTypes.ZOMBIE, HOME.offset(8, 0, 0)); zombie.setNoAi(true); zombie.setPersistenceRequired();
        var player = QaPlayer.join(h.getLevel());
        BlockPos line = h.absolutePos(HOME.offset(4, 0, 0));
        player.snapTo(line.getX() + 0.5, line.getY(), line.getZ() + 0.5, 0, 0);
        float health = zombie.getHealth();
        synchronized (HEARD) { HEARD.clear(); }
        recording = true;
        h.startSequence().thenIdle(80).thenExecute(() -> {
            h.assertTrue(zombie.getHealth() == health, "holds fire while a player stands in the line of fire");
            BlockPos aside = h.absolutePos(HOME.offset(4, 0, 5));
            player.snapTo(aside.getX() + 0.5, aside.getY(), aside.getZ() + 0.5, 0, 0);
        }).thenWaitUntil(() -> h.assertTrue(zombie.getHealth() < health, "fires and hits at range once the line is clear")).thenExecute(() -> {
            recording = false;
            List<Heard> heard;
            synchronized (HEARD) { heard = List.copyOf(HEARD); }
            h.assertTrue(heard.stream().anyMatch(s -> s.sound() == StandAndHoldSounds.RIFLE_FIRE.get() && s.at().distanceToSqr(npc.position()) < 4),
                    "the rifle report plays at the shooter: " + heard.size());
            h.assertTrue(npc.distanceToSqr(zombie) > 9 && npc.getMainHandItem().is(EquipmentRegistry.SERVICE_RIFLE.get()),
                    "a ranged shot, and the rifle is kept while the launcher is locked");
            TierTests.unlock(h);
        }).thenWaitUntil(() -> h.assertTrue(npc.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()), "the launcher replaces the issued rifle once it unlocks"))
                .thenSucceed();
    }
    /** The real item path: a survival player fires the launcher and the server plays the original launch sound, not the old snowball throw. */
    private static void launcher(GameTestHelper h) {
        QaWorld.begin(h);
        var player = QaPlayer.join(h.getLevel());
        BlockPos pos = h.absolutePos(HOME);
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(EquipmentRegistry.PROTOTYPE.get()));
        synchronized (HEARD) { HEARD.clear(); }
        recording = true;
        try {
            h.assertTrue(player.getMainHandItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND) instanceof InteractionResult.Success, "the launcher fires");
        } finally { recording = false; }
        List<Heard> heard;
        synchronized (HEARD) { heard = List.copyOf(HEARD); }
        var near = heard.stream().filter(s -> s.at().distanceToSqr(player.position()) < 4).toList();
        h.assertTrue(near.stream().anyMatch(s -> s.sound() == StandAndHoldSounds.LAUNCHER_FIRE.get()), "original launch sound plays at the shooter: " + near.size());
        h.assertTrue(near.stream().noneMatch(s -> s.sound() == SoundEvents.SNOWBALL_THROW), "the vanilla stand-in is gone");
        h.succeed();
    }
}
