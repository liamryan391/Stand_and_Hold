package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.progression.ProgressionRules;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class HumanProgressionConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends Number>> STAGE_THRESHOLDS = BUILDER
            .comment("Minimum human points for stages 0 through 6. Exactly seven strictly increasing integers; first must be 0.",
                    "Range: 0 through 9223372036854775807. Invalid lists are reset to defaults by NeoForge.",
                    "Restart the world/server after editing. Stage is recomputed from points; setstage sets the selected minimum.")
            .worldRestart()
            .define("stageThresholds", ProgressionRules.DEFAULT_THRESHOLDS, ProgressionRules::validThresholds);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private HumanProgressionConfig() {
    }

    public static long[] thresholds() {
        return ProgressionRules.thresholds(STAGE_THRESHOLDS.get());
    }
}
