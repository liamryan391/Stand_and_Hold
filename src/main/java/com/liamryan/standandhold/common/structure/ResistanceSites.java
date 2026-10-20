package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public record ResistanceSites() implements Feature {
    public static final MapCodec<ResistanceSites> CODEC = MapCodec.unit(ResistanceSites::new);
    private static final DeferredRegister<MapCodec<? extends Feature>> TYPES = DeferredRegister.create(Registries.FEATURE_TYPE, StandAndHoldConstants.MOD_ID);
    static { TYPES.register("resistance_sites", () -> CODEC); }
    public static void register(IEventBus bus) { TYPES.register(bus); }
    @Override public MapCodec<ResistanceSites> codec() { return CODEC; }
    @Override public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int x = origin.getX() >> 4, z = origin.getZ() >> 4;
        // /place feature must not accidentally bypass candidate/safety policy; explicit admin commands own debug placement.
        if (!(level instanceof WorldGenRegion region) || region.getCenter().x() != x || region.getCenter().z() != z) { return false; }
        if (!StructureConfig.DIMENSIONS.get().contains(level.getLevel().dimension().identifier().toString())) { return false; }
        if (StructureConfig.MAIN_BASES.get() && StructurePlan.candidate(level.getSeed(), x, z,
                StructureConfig.MAIN_BASE_SPACING.get(), StructureConfig.MAIN_BASE_CHANCE.get(), StructureKind.MAIN_BASE.salt())) {
            if (StructurePlacement.place(level, StructureKind.MAIN_BASE, x, z).success()) { return true; }
        }
        return StructureConfig.CHECKPOINTS.get() && StructurePlan.candidate(level.getSeed(), x, z,
                StructureConfig.CHECKPOINT_SPACING.get(), StructureConfig.CHECKPOINT_CHANCE.get(), StructureKind.CHECKPOINT.salt())
                && StructurePlacement.place(level, StructureKind.CHECKPOINT, x, z).success();
    }
}
