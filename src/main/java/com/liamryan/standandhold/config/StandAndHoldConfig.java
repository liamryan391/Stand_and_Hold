package com.liamryan.standandhold.config;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;

import java.util.Locale;

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

    @Config.Name("Point Sources")
    @Config.Comment("Settings for sources that award human progression points.")
    public static final PointSources pointSources = new PointSources();

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

    public static int getParasiteKillReward(ResourceLocation entityRegistryId) {
        if (!pointSources.enableParasiteKillPoints || entityRegistryId == null) {
            return 0;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        String[] rewardEntries = pointSources.parasiteKillRewards;
        if (rewardEntries == null) {
            return 0;
        }

        for (String rewardEntry : rewardEntries) {
            ParsedReward parsedReward = parseRewardEntry(rewardEntry);
            if (parsedReward != null && parsedReward.entityId.equals(entityId)) {
                return parsedReward.points;
            }
        }

        return 0;
    }

    public static boolean isScapeAndRunParasitesLoaded() {
        String modId = pointSources.scapeAndRunParasitesModId;
        return modId != null && !modId.trim().isEmpty() && Loader.isModLoaded(modId.trim());
    }

    private static ParsedReward parseRewardEntry(String rewardEntry) {
        if (rewardEntry == null) {
            return null;
        }

        int separator = rewardEntry.indexOf('=');
        if (separator <= 0 || separator >= rewardEntry.length() - 1) {
            return null;
        }

        String entityId = normalizeEntityId(rewardEntry.substring(0, separator));
        if (entityId == null) {
            return null;
        }

        try {
            int points = Integer.parseInt(rewardEntry.substring(separator + 1).trim());
            return points > 0 ? new ParsedReward(entityId, points) : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String normalizeEntityId(String rawEntityId) {
        if (rawEntityId == null || rawEntityId.trim().isEmpty()) {
            return null;
        }

        try {
            return new ResourceLocation(rawEntityId.trim().toLowerCase(Locale.ROOT)).toString();
        } catch (RuntimeException ignored) {
            return null;
        }
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

    public static final class PointSources {
        @Config.Name("Enable Parasite Kill Points")
        @Config.Comment("Awards human progression points when configured entity registry IDs die.")
        public boolean enableParasiteKillPoints = true;

        @Config.Name("Parasite Kill Rewards")
        @Config.Comment({
                "Entity registry IDs and human point rewards for parasite/test kills.",
                "Use the format modid:entity_registry_name=points.",
                "The default minecraft:zombie entry is only a safe test value for early development."
        })
        public String[] parasiteKillRewards = new String[] {
                "minecraft:zombie=5"
        };

        @Config.Name("Scape and Run Parasites Mod ID")
        @Config.Comment({
                "Optional mod id used only to detect whether Scape and Run: Parasites is loaded.",
                "No SRP entity IDs are hardcoded; add verified SRP entity registry IDs to Parasite Kill Rewards."
        })
        public String scapeAndRunParasitesModId = "srparasites";
    }

    private static final class ParsedReward {
        private final String entityId;
        private final int points;

        private ParsedReward(String entityId, int points) {
            this.entityId = entityId;
            this.points = points;
        }
    }
}
