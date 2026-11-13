package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.StandAndHoldConstants;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

/** M33 presentation: the original sound events are registered and the server plays them where gameplay used vanilla stand-ins. */
final class PresentationTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    private PresentationTests() { }

    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("presentation_sounds_registered", PresentationTests::registered);
        tests.put("presentation_launcher_sound", PresentationTests::launcher);
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
                "post.radio", StandAndHoldSounds.POST_RADIO.get(), "post.radio_urgent", StandAndHoldSounds.POST_RADIO_URGENT.get());
        expected.forEach((path, sound) -> {
            var id = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, path);
            h.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(id) && BuiltInRegistries.SOUND_EVENT.getValue(id) == sound, "registered: " + id);
            h.assertTrue(sound.location().equals(id), "event names its own sounds.json entry: " + id);
        });
        h.succeed();
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
