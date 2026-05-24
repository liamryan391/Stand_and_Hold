package com.liamryan.standandhold.config;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

@Config(modid = StandAndHoldConstants.MOD_ID, name = StandAndHoldConstants.MOD_ID)
public final class StandAndHoldConfig {
    private static final int[] DEFAULT_STAGE_THRESHOLDS = new int[] {
            0,
            100,
            300,
            700,
            1500,
            3000,
            6000
    };

    @Config.Name("Debug Logging")
    @Config.Comment("Enables extra Stand and Hold log messages while developing the mod.")
    public static boolean debugLogging = false;

    @Config.Name("Progression")
    @Config.Comment("Human-side escalation point settings.")
    public static final Progression progression = new Progression();

    private StandAndHoldConfig() {
    }

    public static void sync() {
        ConfigManager.sync(StandAndHoldConstants.MOD_ID, Config.Type.INSTANCE);
    }

    public static int getStageThreshold(int stageId) {
        int[] thresholds = progression.stageThresholds;
        if (thresholds == null || thresholds.length == 0) {
            thresholds = DEFAULT_STAGE_THRESHOLDS;
        }

        int thresholdIndex = Math.min(Math.max(stageId, 0), thresholds.length - 1);
        return Math.max(0, thresholds[thresholdIndex]);
    }

    public static final class Progression {
        @Config.Name("Stage Thresholds")
        @Config.Comment({
                "Human point thresholds for stages 0 through 6.",
                "Index 0 is Stage 0, index 1 is Stage 1, and so on.",
                "If fewer than 7 values are provided, the last value is reused for higher stages."
        })
        public int[] stageThresholds = DEFAULT_STAGE_THRESHOLDS.clone();
    }
}
