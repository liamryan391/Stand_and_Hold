package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;

/** Virtual storage only; physical sample insertion is supplied by M5. */
public record BuildingState(int level, long supplies, int samples, String target, int work) {
    public static final int MAX_SAMPLES = 4096;
    public static final Codec<BuildingState> CODEC = CompoundTag.CODEC.xmap(BuildingState::load, BuildingState::save);
    public static final BuildingState EMPTY = new BuildingState(1, 0, 0, "", 0);

    public BuildingState {
        if (level < 1 || level > 5 || supplies < 0 || samples < 0 || samples > MAX_SAMPLES || work < 0) {
            throw new IllegalArgumentException("Invalid building state.");
        }
        if (!target.isEmpty()) { FoundationCatalog.requireId(target); }
    }

    public BuildingState supplies(long amount) { return new BuildingState(level, amount, samples, target, work); }
    public BuildingState target(String id) { return new BuildingState(level, supplies, samples, id, 0); }
    public BuildingState work(int amount) { return new BuildingState(level, supplies, samples, target, amount); }
    public BuildingState insertSamples(int amount) {
        if (amount <= 0 || amount > MAX_SAMPLES - samples) { throw new IllegalArgumentException("Sample storage full or invalid amount."); }
        return new BuildingState(level, supplies, samples + amount, target, work);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putLong("supplies", supplies);
        tag.putInt("samples", samples);
        tag.putString("target", target);
        tag.putInt("work", work);
        return tag;
    }

    private static BuildingState load(CompoundTag tag) {
        String target = tag.getStringOr("target", "");
        try { if (!target.isEmpty()) { FoundationCatalog.requireId(target); } }
        catch (IllegalArgumentException exception) { target = ""; }
        return new BuildingState(Math.clamp(tag.getIntOr("level", 1), 1, 5),
                Math.max(0, tag.getLongOr("supplies", 0)), Math.clamp(tag.getIntOr("samples", 0), 0, MAX_SAMPLES),
                target, target.isEmpty() ? 0 : Math.max(0, tag.getIntOr("work", 0)));
    }
}
