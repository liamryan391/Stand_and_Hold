package com.liamryan.standandhold.common.sound;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Original procedurally synthesised sounds (scripts/generate-sounds.py); see assets/standandhold/sounds.json. */
public final class StandAndHoldSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, StandAndHoldConstants.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_GEAR = sound("npc.gear");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAUNCHER_FIRE = sound("launcher.fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> POST_RADIO = sound("post.radio");
    private StandAndHoldSounds() { }
    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, name)));
    }
    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
