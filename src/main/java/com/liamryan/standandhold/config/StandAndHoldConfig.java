package com.liamryan.standandhold.config;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
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

    @Config.Name("Research")
    @Config.Comment("Data-driven research entries and rewards.")
    public static final Research research = new Research();

    @Config.Name("Infrastructure")
    @Config.Comment("Settings for military infrastructure blocks.")
    public static final Infrastructure infrastructure = new Infrastructure();

    @Config.Name("Human NPCs")
    @Config.Comment("Settings for early human NPC entities.")
    public static final HumanNpcs humanNpcs = new HumanNpcs();

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

    public static double getParasiteSampleDropChance(ResourceLocation entityRegistryId) {
        if (!pointSources.enableParasiteSampleDrops || entityRegistryId == null) {
            return 0.0D;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        String[] dropEntries = pointSources.parasiteSampleDropChances;
        if (dropEntries == null) {
            return 0.0D;
        }

        for (String dropEntry : dropEntries) {
            ParsedChance parsedChance = parseChanceEntry(dropEntry);
            if (parsedChance != null && parsedChance.entityId.equals(entityId)) {
                return parsedChance.chance;
            }
        }

        return 0.0D;
    }

    public static boolean isScapeAndRunParasitesLoaded() {
        String modId = pointSources.scapeAndRunParasitesModId;
        return modId != null && !modId.trim().isEmpty() && Loader.isModLoaded(modId.trim());
    }

    public static boolean isHumanUnitTargetEntity(ResourceLocation entityRegistryId) {
        if (!humanNpcs.enableHumanUnitParasiteTargeting || entityRegistryId == null) {
            return false;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        String[] targetEntries = humanNpcs.humanUnitTargetEntityIds;
        if (targetEntries == null) {
            return false;
        }

        for (String targetEntry : targetEntries) {
            String configuredEntityId = normalizeEntityId(targetEntry);
            if (configuredEntityId != null && configuredEntityId.equals(entityId)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSoldierTargetEntity(ResourceLocation entityRegistryId) {
        return isHumanUnitTargetEntity(entityRegistryId);
    }

    public static double getHumanUnitHealth(HumanUnitTier tier) {
        ParsedHumanUnitStats stats = getHumanUnitStats(tier);
        return Math.max(1.0D, stats.health);
    }

    public static double getHumanUnitDamage(HumanUnitTier tier) {
        ParsedHumanUnitStats stats = getHumanUnitStats(tier);
        return Math.max(0.0D, stats.damage);
    }

    public static int getHumanUnitRequiredStage(HumanUnitTier tier) {
        ParsedHumanUnitStats stats = getHumanUnitStats(tier);
        return Math.max(0, Math.min(6, stats.requiredStage));
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

    private static ParsedChance parseChanceEntry(String chanceEntry) {
        if (chanceEntry == null) {
            return null;
        }

        int separator = chanceEntry.indexOf('=');
        if (separator <= 0 || separator >= chanceEntry.length() - 1) {
            return null;
        }

        String entityId = normalizeEntityId(chanceEntry.substring(0, separator));
        if (entityId == null) {
            return null;
        }

        try {
            double chance = Double.parseDouble(chanceEntry.substring(separator + 1).trim());
            return chance > 0.0D ? new ParsedChance(entityId, Math.min(chance, 1.0D)) : null;
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

    private static ParsedHumanUnitStats getHumanUnitStats(HumanUnitTier tier) {
        HumanUnitTier safeTier = tier == null ? HumanUnitTier.SURVIVOR_DEFENDER : tier;
        String[] statsEntries = humanNpcs.humanUnitStats;
        if (statsEntries != null) {
            for (String statsEntry : statsEntries) {
                ParsedHumanUnitStats parsedStats = parseHumanUnitStats(statsEntry);
                if (parsedStats != null && parsedStats.tier == safeTier) {
                    return parsedStats;
                }
            }
        }

        return ParsedHumanUnitStats.defaults(safeTier);
    }

    private static ParsedHumanUnitStats parseHumanUnitStats(String statsEntry) {
        if (statsEntry == null || statsEntry.trim().isEmpty()) {
            return null;
        }

        String[] parts = statsEntry.split("\\|", -1);
        if (parts.length < 4) {
            return null;
        }

        HumanUnitTier tier = HumanUnitTier.fromId(parts[0]);
        if (tier == null) {
            return null;
        }

        try {
            double health = Double.parseDouble(parts[1].trim());
            double damage = Double.parseDouble(parts[2].trim());
            int requiredStage = Integer.parseInt(parts[3].trim());
            return new ParsedHumanUnitStats(tier, health, damage, requiredStage);
        } catch (NumberFormatException ignored) {
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

        @Config.Name("Enable Parasite Sample Drops")
        @Config.Comment("Allows configured entity registry IDs to drop Parasite Tissue Samples.")
        public boolean enableParasiteSampleDrops = true;

        @Config.Name("Parasite Sample Drop Chances")
        @Config.Comment({
                "Entity registry IDs and sample drop chances for parasite/test kills.",
                "Use the format modid:entity_registry_name=chance, where chance is 0.0 to 1.0.",
                "The default minecraft:zombie entry is only a safe test value for early development."
        })
        public String[] parasiteSampleDropChances = new String[] {
                "minecraft:zombie=0.25"
        };

        @Config.Name("Scape and Run Parasites Mod ID")
        @Config.Comment({
                "Optional mod id used only to detect whether Scape and Run: Parasites is loaded.",
                "No SRP entity IDs are hardcoded; add verified SRP entity registry IDs to Parasite Kill Rewards."
        })
        public String scapeAndRunParasitesModId = "srparasites";
    }

    public static final class Research {
        @Config.Name("Enable Research Lab Progress")
        @Config.Comment("Allows loaded Research Labs to generate progress toward the first available incomplete research entry.")
        public boolean enableResearchLabProgress = true;

        @Config.Name("Research Lab Tick Interval")
        @Config.Comment("Ticks between passive research progress from each loaded Research Lab. 200 ticks is about 10 seconds.")
        public int researchLabTickInterval = 200;

        @Config.Name("Research Lab Progress Per Interval")
        @Config.Comment("Progress added by each loaded Research Lab per interval.")
        public int researchLabProgressPerInterval = 10;

        @Config.Name("Research Lab Progress Required")
        @Config.Comment("Progress required for a Research Lab to complete one available research entry.")
        public int researchLabProgressRequired = 100;

        @Config.Name("Research Lab Max Stored Samples")
        @Config.Comment("Maximum Parasite Tissue Samples a Research Lab can store for research completion costs.")
        public int researchLabMaxStoredSamples = 16;

        @Config.Name("Research Entries")
        @Config.Comment({
                "Research entries in the format id|category|name|description|pointReward|requiredResearchIds|sampleCost.",
                "Use comma-separated requiredResearchIds, or leave that field blank.",
                "Use 0 for sampleCost when the research should not consume Parasite Tissue Samples.",
                "Valid default categories are GENERAL, PARASITE_BIOLOGY, MILITARY_LOGISTICS, BASE_INFRASTRUCTURE, FIELD_MEDICINE, and SPECIAL_PROJECTS."
        })
        public String[] researchEntries = new String[] {
                "parasite_samples|PARASITE_BIOLOGY|Parasite Samples|Catalog recovered parasite tissue and establish basic containment procedures.|25||1",
                "field_communications|MILITARY_LOGISTICS|Field Communications|Coordinate survivor cells and local army response teams across infected territory.|25||0",
                "outpost_doctrine|BASE_INFRASTRUCTURE|Outpost Doctrine|Draft the first defensible outpost standards for later military construction.|50|field_communications|0"
        };
    }

    public static final class Infrastructure {
        @Config.Name("Enable Field Command Post Point Generation")
        @Config.Comment("Allows loaded Field Command Posts to generate human progression points over time.")
        public boolean enableFieldCommandPostPointGeneration = true;

        @Config.Name("Field Command Post Points Per Interval")
        @Config.Comment("Human points generated by each loaded Field Command Post per interval.")
        public int fieldCommandPostPointsPerInterval = 1;

        @Config.Name("Field Command Post Tick Interval")
        @Config.Comment("Ticks between passive point payouts from each loaded Field Command Post. 1200 ticks is about 60 seconds.")
        public int fieldCommandPostTickInterval = 1200;

        @Config.Name("Field Command Post Upgrade Requirements")
        @Config.Comment({
                "Upgrade requirements in the format targetLevel|requiredHumanPoints|requiredResearchIds|parasiteSampleCost|pointReward.",
                "targetLevel must be 2 through 5. Level names are fixed by the mod.",
                "Use comma-separated requiredResearchIds, or leave that field blank.",
                "Human points are required as progression, not spent. Parasite samples are consumed from the upgrading player."
        })
        public String[] fieldCommandPostUpgradeRequirements = new String[] {
                "2|100|field_communications|1|25",
                "3|300|outpost_doctrine|2|50",
                "4|700|outpost_doctrine,parasite_samples|4|100",
                "5|1500|outpost_doctrine,parasite_samples|8|200"
        };

        @Config.Name("Enable Outpost Defender Spawning")
        @Config.Comment("Allows loaded Field Command Posts to act as outpost anchors that spawn limited human defenders.")
        public boolean enableOutpostDefenderSpawning = true;

        @Config.Name("Outpost Max Defenders")
        @Config.Comment("Maximum living defenders assigned to each loaded outpost anchor.")
        public int outpostMaxDefenders = 3;

        @Config.Name("Outpost Defender Spawn Interval")
        @Config.Comment("Ticks between defender spawn attempts per loaded outpost anchor. 2400 ticks is about 2 minutes.")
        public int outpostDefenderSpawnInterval = 2400;

        @Config.Name("Outpost Defender Patrol Radius")
        @Config.Comment("Radius assigned defenders try to stay within around their outpost anchor.")
        public int outpostDefenderPatrolRadius = 16;

        @Config.Name("Outpost Defender Spawn Search Radius")
        @Config.Comment("Horizontal radius around the outpost anchor used to find a safe defender spawn position.")
        public int outpostDefenderSpawnSearchRadius = 4;
    }

    public static final class HumanNpcs {
        @Config.Name("Enable Human Unit Parasite Targeting")
        @Config.Comment("Allows human combat units to attack configured parasite/test entity registry IDs.")
        public boolean enableHumanUnitParasiteTargeting = true;

        @Config.Name("Human Unit Target Entity IDs")
        @Config.Comment({
                "Entity registry IDs human combat units are allowed to target.",
                "Use verified parasite registry IDs here. The default minecraft:zombie entry is only a safe test value.",
                "Players and Stand and Hold human NPCs are always ignored by default."
        })
        public String[] humanUnitTargetEntityIds = new String[] {
                "minecraft:zombie"
        };

        @Config.Name("Human Unit Stats")
        @Config.Comment({
                "Human unit tier stats in the format unitId|health|damage|requiredHumanStage.",
                "Valid unit IDs: survivor_defender, army_rifleman, heavy_soldier, elite_soldier, super_elite_soldier, special_parasite_division_operative.",
                "Required human stage gates spawning for that unit tier."
        })
        public String[] humanUnitStats = new String[] {
                "survivor_defender|16|2|0",
                "army_rifleman|20|4|1",
                "heavy_soldier|28|6|2",
                "elite_soldier|36|8|3",
                "super_elite_soldier|48|11|4",
                "special_parasite_division_operative|60|14|5"
        };

        @Config.Name("Base Human NPC Movement Speed")
        @Config.Comment("Base movement speed for early human NPCs.")
        public double baseHumanNpcMovementSpeed = 0.28D;

        @Config.Name("Base Human NPC Follow Range")
        @Config.Comment("Target search and awareness range for early human NPCs.")
        public double baseHumanNpcFollowRange = 24.0D;

        @Config.Name("Human Unit Attack Move Speed")
        @Config.Comment("Movement speed multiplier used by human combat units while attacking.")
        public double humanUnitAttackMoveSpeed = 1.1D;

        @Config.Name("Human Unit Wander Speed")
        @Config.Comment("Movement speed multiplier used by human combat units while wandering.")
        public double humanUnitWanderSpeed = 0.8D;

        @Config.Name("Human Unit Target Chance")
        @Config.Comment("How often human combat units run nearest-target checks. Lower values react faster; 10 is the vanilla-style default.")
        public int humanUnitTargetChance = 10;
    }

    private static final class ParsedReward {
        private final String entityId;
        private final int points;

        private ParsedReward(String entityId, int points) {
            this.entityId = entityId;
            this.points = points;
        }
    }

    private static final class ParsedChance {
        private final String entityId;
        private final double chance;

        private ParsedChance(String entityId, double chance) {
            this.entityId = entityId;
            this.chance = chance;
        }
    }

    private static final class ParsedHumanUnitStats {
        private final HumanUnitTier tier;
        private final double health;
        private final double damage;
        private final int requiredStage;

        private ParsedHumanUnitStats(HumanUnitTier tier, double health, double damage, int requiredStage) {
            this.tier = tier;
            this.health = health;
            this.damage = damage;
            this.requiredStage = requiredStage;
        }

        private static ParsedHumanUnitStats defaults(HumanUnitTier tier) {
            return new ParsedHumanUnitStats(tier, tier.getDefaultHealth(), tier.getDefaultDamage(), tier.getDefaultRequiredStage());
        }
    }
}
