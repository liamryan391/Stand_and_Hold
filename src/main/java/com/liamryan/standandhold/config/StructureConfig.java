package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class StructureConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DIMENSIONS = B
            .comment("Allowed dimension registry IDs for natural AND debug placement. Natural sites also need the biome modifier (default: Overworld biomes). Empty disables all placement; unknown IDs never match.")
            .define("allowedDimensions", List.of("minecraft:overworld"), StructureConfig::dimensions);
    public static final ModConfigSpec.BooleanValue CHECKPOINTS = B.comment("Generate checkpoints in NEW chunks only. Disabling does not remove existing sites; admin debug placement still works.")
            .define("checkpointsEnabled", true);
    public static final ModConfigSpec.IntValue CHECKPOINT_SPACING = B.comment("One deterministic candidate per square region of this many chunks. Not a guaranteed spawn; chance and terrain checks still apply.")
            .defineInRange("checkpointSpacingChunks", 12, 1, 256);
    public static final ModConfigSpec.IntValue CHECKPOINT_CHANCE = B.comment("Candidate succeeds 1 in N times before terrain checks. 1 always attempts; no search/retry in other chunks.")
            .defineInRange("checkpointChance", 2, 1, 10000);
    public static final ModConfigSpec.IntValue CHECKPOINT_RADIUS = B.comment("Checkpoint radius in blocks: footprint is 2r+1 (default 9x9). Always remains inside one chunk.")
            .defineInRange("checkpointRadius", 4, 3, 5);
    public static final ModConfigSpec.BooleanValue MAIN_BASES = B.comment("Generate rare Main Base foundations in new chunks. Main Base candidates take priority over checkpoints in the same chunk.")
            .define("mainBasesEnabled", true);
    public static final ModConfigSpec.IntValue MAIN_BASE_SPACING = B.comment("Main Base candidate-region size in chunks; same deterministic/no-retry policy as checkpoints.")
            .defineInRange("mainBaseSpacingChunks", 64, 1, 256);
    public static final ModConfigSpec.IntValue MAIN_BASE_CHANCE = B.comment("Main Base candidate attempt probability: 1 in N, then strict terrain checks.")
            .defineInRange("mainBaseChance", 4, 1, 10000);
    public static final ModConfigSpec.IntValue MAIN_BASE_RADIUS = B.comment("Main Base foundation radius: footprint 11x11 or 13x13, within one chunk.")
            .defineInRange("mainBaseRadius", 6, 5, 6);
    public static final ModConfigSpec.IntValue ACTIVATION_STAGE = B.comment("Stage needed for Main Base COMMAND POST generation/deployment. Rechecked at existing building intervals; lowering the human stage makes it dormant again. Lab/crate menus remain usable.")
            .defineInRange("mainBaseActivationStage", 5, 0, 6);
    public static final ModConfigSpec.IntValue SLOPE = B.comment("Maximum surface height difference across the entire footprint. Foundations fill only this bounded gap; no excavation, fluids, trees, buildings or block entities are overwritten.")
            .defineInRange("maximumHeightDifference", 2, 0, 3);
    public static final ModConfigSpec.IntValue CHECKPOINT_SUPPLIES = B.comment("Initial LOCAL crate supplies per new checkpoint. Not added again on reload. No samples or equipment are supplied.")
            .defineInRange("checkpointCrateSupplies", 2, 0, 100);
    public static final ModConfigSpec.IntValue MAIN_BASE_SUPPLIES = B.comment("Initial local crate supplies per new Main Base, bounded by building capacity.")
            .defineInRange("mainBaseCrateSupplies", 5, 0, 100);
    public static final ModConfigSpec.BooleanValue EXPANSION = B.comment("EXPERIMENTAL master switch, off by default: allows menu/manual/operator expansion and per-site Automatic opt-in for proven radius-4 level-2+ checkpoints only. Replaces 88 surrounding ground cells; matching player dirt/grass cannot be identified. Not claim-mod approval or crash-atomic recovery. Back up the whole world. No automatic level purchase, repair, capacity increase or resource charge/reward.")
            .define("expansionEnabled", false);
    public static final ModConfigSpec SPEC = B.build();

    private StructureConfig() { }
    private static boolean dimensions(Object raw) {
        if (!(raw instanceof List<?> values) || values.size() > 32) { return false; }
        for (Object value : values) {
            if (!(value instanceof String id)) { return false; }
            try { FoundationCatalog.requireId(id); } catch (IllegalArgumentException exception) { return false; }
        }
        return true;
    }
}
