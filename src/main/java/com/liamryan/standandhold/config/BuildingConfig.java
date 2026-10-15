package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
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
    public static final ModConfigSpec.IntValue CAPACITY = B.comment("Local supply capacity for all buildings. Lowering this does not delete existing supplies; deposits/generation stop until below cap.")
            .defineInRange("supplyCapacity", 2000, 1, 1000000);
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
            .comment("Local samples spent for upgrades to levels 2, 3, 4, 5. Physical insertion arrives in M5.")
            .define("upgradeSampleCosts", List.of(0, 0, 2, 5), BuildingConfig::fourCosts);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> UPGRADE_RESEARCH = B
            .comment("Research IDs required for levels 2, 3, 4, 5; empty string means none. Four entries; unknown IDs block safely.")
            .define("upgradeResearch", List.of("standandhold:parasite_samples", "standandhold:field_communications",
                    "standandhold:field_communications", "standandhold:special_division_training"), BuildingConfig::fourIds);
    public static final ModConfigSpec SPEC = B.build();

    private BuildingConfig() { }
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
