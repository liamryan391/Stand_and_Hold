package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingStorage;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class BuildingConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue INTERVAL = B.comment("Loaded command-post ticks per generation cycle. No offline catch-up. 0 rewards disable that source.")
            .defineInRange("generationIntervalTicks", 1200, 20, 72000);
    public static final ModConfigSpec.IntValue POINTS = B.comment("Human points per command-post cycle, independent of level.")
            .defineInRange("pointsPerInterval", 1, 0, 10000);
    public static final ModConfigSpec.IntValue SUPPLIES = B.comment("Local supplies per command-post cycle; stops at storage capacity.")
            .defineInRange("suppliesPerInterval", 1, 0, 10000);
    public static final ModConfigSpec.IntValue CAPACITY = B.comment("Flat local supply capacity. Used by every building unless levelled Command Post storage is explicitly enabled; always used by labs/crates. Lowering limits never deletes supplies; over-cap export remains available.")
            .defineInRange("supplyCapacity", 2000, 1, 1000000);
    public static final ModConfigSpec.BooleanValue LEVEL_STORAGE = B.comment("Opt in to level-aware Command Post storage only. Default false preserves all deliberate flat-cap settings, including existing 2000 values. No refill, generation-rate change or migration.")
            .define("levelledCommandPostStorage", false);
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> STORAGE_LEVELS = B.comment("Command Post capacities at local levels 1-5 when levelledCommandPostStorage is true. Exactly five non-decreasing whole numbers, 1-1000000. Labs/crates retain supplyCapacity. Invalid in-memory profiles fall back to the flat cap.")
            .define("commandPostSupplyCapacities", BuildingStorage.DEFAULT_LEVELS, BuildingStorage::validProfile);
    public static final ModConfigSpec.IntValue TRANSFER = B.comment("Fixed GUI batch size for global/local supply transfers; insufficient balance/capacity rejects the whole transfer.")
            .defineInRange("transferBatch", 10, 1, 10000);
    public static final ModConfigSpec.IntValue LAB_INTERVAL = B.comment("Loaded ticks per lab work unit. Blocked labs do not consume costs or gain work.")
            .defineInRange("labIntervalTicks", 100, 20, 72000);
    public static final ModConfigSpec.IntValue LAB_WORK = B.comment("Work units needed per research. Costs are checked every interval and spent once at completion.")
            .defineInRange("labWorkRequired", 12, 1, 1000000);
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> UPGRADE_POINTS = B
            .comment("Minimum human points (not spent) for upgrades to levels 2, 3, 4, 5.")
            .define("upgradePointRequirements", List.of(150, 500, 1200, 5200), BuildingConfig::fourCosts);
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> UPGRADE_SUPPLIES = B
            .comment("Local supplies spent for upgrades to levels 2, 3, 4, 5.")
            .define("upgradeSupplyCosts", List.of(20, 50, 100, 200), BuildingConfig::fourCosts);
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> UPGRADE_SAMPLES = B
            .comment("Local samples spent for upgrades to levels 2, 3, 4, 5. Use Samples mode in the building menu to store physical items.")
            .define("upgradeSampleCosts", List.of(0, 0, 2, 5), BuildingConfig::fourCosts);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> UPGRADE_RESEARCH = B
            .comment("Research IDs required for levels 2, 3, 4, 5; empty string means none. Four entries; unknown IDs block safely.")
            .define("upgradeResearch", List.of("standandhold:parasite_samples", "standandhold:field_communications",
                    "standandhold:field_communications", "standandhold:special_division_training"), BuildingConfig::fourIds);
    public static final ModConfigSpec SPEC = B.build();

    private BuildingConfig() { }
    public static int capacity(BuildingKind kind, int level) {
        return BuildingStorage.capacity(kind, level, CAPACITY.get(), LEVEL_STORAGE.get(), STORAGE_LEVELS.get());
    }
    private static boolean fourCosts(Object raw) {
        return raw instanceof List<?> values && values.size() == 4 && values.stream().allMatch(value -> value instanceof Number n
                && n.doubleValue() == n.longValue() && n.longValue() >= 0 && n.longValue() <= 1000000);
    }
    private static boolean fourIds(Object raw) {
        if (!(raw instanceof List<?> values) || values.size() != 4) { return false; }
        for (Object value : values) {
            if (!(value instanceof String id)) { return false; }
            try { if (!id.isEmpty()) { FoundationCatalog.requireId(id); } }
            catch (IllegalArgumentException exception) { return false; }
        }
        return true;
    }
}
