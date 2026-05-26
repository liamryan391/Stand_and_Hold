package com.liamryan.standandhold.config;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.compat.SRPCompat;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import java.util.Locale;

@Config(modid = StandAndHoldConstants.MOD_ID, name = StandAndHoldConstants.MOD_ID)
public final class StandAndHoldConfig {
    private static final int[] DEFAULT_STAGE_THRESHOLDS = new int[] {
            0,
            150,
            500,
            1200,
            2600,
            5200,
            10000
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

    @Config.Name("Compatibility")
    @Config.Comment("Optional integration settings for other mods. These settings never create a hard dependency.")
    public static final Compatibility compatibility = new Compatibility();

    @Config.Name("Research")
    @Config.Comment("Data-driven research entries and rewards.")
    public static final Research research = new Research();

    @Config.Name("Missions")
    @Config.Comment("Basic mission and objective definitions.")
    public static final Missions missions = new Missions();

    @Config.Name("Infrastructure")
    @Config.Comment("Settings for military infrastructure blocks.")
    public static final Infrastructure infrastructure = new Infrastructure();

    @Config.Name("Supply")
    @Config.Comment("Settings for the basic human supply economy.")
    public static final Supply supply = new Supply();

    @Config.Name("Equipment")
    @Config.Comment("Settings for the first simple human equipment items.")
    public static final Equipment equipment = new Equipment();

    @Config.Name("Human NPCs")
    @Config.Comment("Settings for early human NPC entities.")
    public static final HumanNpcs humanNpcs = new HumanNpcs();

    @Config.Name("World Generation")
    @Config.Comment("Settings for generated Stand and Hold structures.")
    public static final WorldGeneration worldGeneration = new WorldGeneration();

    @Config.Name("Threat Response")
    @Config.Comment("Settings for regional threat tracking and reinforcement decisions.")
    public static final ThreatResponse threatResponse = new ThreatResponse();

    @Config.Name("Dynamic Events")
    @Config.Comment("Settings for lightweight outpost attacks and reinforcement events.")
    public static final DynamicEvents dynamicEvents = new DynamicEvents();

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
            return SRPCompat.getKillReward(entityRegistryId);
        }

        for (String rewardEntry : rewardEntries) {
            ParsedReward parsedReward = parseRewardEntry(rewardEntry);
            if (parsedReward != null && parsedReward.entityId.equals(entityId)) {
                return parsedReward.points;
            }
        }

        return SRPCompat.getKillReward(entityRegistryId);
    }

    public static double getParasiteSampleDropChance(ResourceLocation entityRegistryId) {
        if (!pointSources.enableParasiteSampleDrops || entityRegistryId == null) {
            return 0.0D;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        String[] dropEntries = pointSources.parasiteSampleDropChances;
        if (dropEntries == null) {
            return SRPCompat.getSampleDropChance(entityRegistryId);
        }

        for (String dropEntry : dropEntries) {
            ParsedChance parsedChance = parseChanceEntry(dropEntry);
            if (parsedChance != null && parsedChance.entityId.equals(entityId)) {
                return parsedChance.chance;
            }
        }

        return SRPCompat.getSampleDropChance(entityRegistryId);
    }

    public static boolean isScapeAndRunParasitesLoaded() {
        return SRPCompat.isLoaded();
    }

    public static boolean isHumanUnitTargetEntity(ResourceLocation entityRegistryId) {
        if (!humanNpcs.enableHumanUnitParasiteTargeting || entityRegistryId == null) {
            return false;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        String[] targetEntries = humanNpcs.humanUnitTargetEntityIds;
        if (targetEntries == null) {
            return SRPCompat.isMappedHumanTargetEntity(entityRegistryId);
        }

        for (String targetEntry : targetEntries) {
            String configuredEntityId = normalizeEntityId(targetEntry);
            if (configuredEntityId != null && configuredEntityId.equals(entityId)) {
                return true;
            }
        }

        return SRPCompat.isMappedHumanTargetEntity(entityRegistryId);
    }

    public static boolean isConfiguredParasiteEntity(ResourceLocation entityRegistryId) {
        if (entityRegistryId == null) {
            return false;
        }

        if (isHumanUnitTargetEntity(entityRegistryId)) {
            return true;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        String[] rewardEntries = pointSources.parasiteKillRewards;
        if (rewardEntries != null) {
            for (String rewardEntry : rewardEntries) {
                ParsedReward parsedReward = parseRewardEntry(rewardEntry);
                if (parsedReward != null && parsedReward.entityId.equals(entityId)) {
                    return true;
                }
            }
        }

        String[] sampleEntries = pointSources.parasiteSampleDropChances;
        if (sampleEntries != null) {
            for (String sampleEntry : sampleEntries) {
                ParsedChance parsedChance = parseChanceEntry(sampleEntry);
                if (parsedChance != null && parsedChance.entityId.equals(entityId)) {
                    return true;
                }
            }
        }

        return SRPCompat.isMappedParasiteEntity(entityRegistryId);
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
        int requiredStage = Math.max(0, Math.min(6, stats.requiredStage));
        return tier == HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE ? Math.max(5, requiredStage) : requiredStage;
    }

    public static String getSpecialParasiteDivisionTrainingResearchId() {
        String researchId = humanNpcs.specialParasiteDivisionTrainingResearchId;
        return researchId == null || researchId.trim().isEmpty() ? "special_division_training" : researchId.trim();
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
                "Malformed entries and empty registry IDs are skipped safely.",
                "The default minecraft:zombie entry is only a safe test value for early development.",
                "Use verified SRP registry IDs only after testing the exact SRP build in your pack."
        })
        public String[] parasiteKillRewards = new String[] {
                "minecraft:zombie=3"
        };

        @Config.Name("Enable Parasite Sample Drops")
        @Config.Comment("Allows configured entity registry IDs to drop Parasite Tissue Samples.")
        public boolean enableParasiteSampleDrops = true;

        @Config.Name("Parasite Sample Drop Chances")
        @Config.Comment({
                "Entity registry IDs and sample drop chances for parasite/test kills.",
                "Use the format modid:entity_registry_name=chance, where chance is 0.0 to 1.0.",
                "Malformed entries and empty registry IDs are skipped safely.",
                "The default minecraft:zombie entry is only a safe test value for early development.",
                "Use verified SRP registry IDs only after testing the exact SRP build in your pack."
        })
        public String[] parasiteSampleDropChances = new String[] {
                "minecraft:zombie=0.15"
        };

    }

    public static final class Compatibility {
        @Config.Name("Enable Scape and Run Parasites Compatibility")
        @Config.Comment({
                "Enables optional Scape and Run: Parasites compatibility when that mod is installed.",
                "This only performs Loader.isModLoaded checks and configurable registry ID matching.",
                "Stand and Hold does not reference SRP classes, so SRP remains optional.",
                "If SRP is not installed, SRP-specific mappings stay dormant."
        })
        public boolean enableScapeAndRunParasitesCompatibility = true;

        @Config.Name("Scape and Run Parasites Mod ID")
        @Config.Comment("Mod id used for the optional SRP loaded check. Change only if your SRP build uses a different mod id.")
        public String scapeAndRunParasitesModId = "srparasites";

        @Config.Name("SRP Parasite Mappings")
        @Config.Comment({
                "Optional SRP entity registry mappings in the format entityId|killReward|sampleDropChance|humanTarget.",
                "Example after verifying an entity id: srparasites:example_parasite|25|0.35|true.",
                "killReward awards human points, sampleDropChance controls Parasite Tissue Sample drops, and humanTarget lets human units attack it.",
                "If humanTarget is omitted, it defaults to true for that mapping.",
                "Malformed entries and empty registry IDs are skipped safely.",
                "No SRP entity IDs are enabled by default until verified against the exact SRP build being used."
        })
        public String[] srpParasiteMappings = new String[0];

        @Config.Name("Enable Verified SRP Default Mappings")
        @Config.Comment({
                "Enables built-in SRP mappings only when this codebase has verified registry IDs.",
                "The current foundation intentionally ships with no built-in SRP entity IDs."
        })
        public boolean enableVerifiedSrpDefaultMappings = false;
    }

    public static final class Research {
        @Config.Name("Enable Research Lab Progress")
        @Config.Comment("Allows loaded Research Labs to generate progress toward the first available incomplete research entry.")
        public boolean enableResearchLabProgress = true;

        @Config.Name("Research Lab Tick Interval")
        @Config.Comment("Ticks between passive research progress from each loaded Research Lab. 400 ticks is about 20 seconds.")
        public int researchLabTickInterval = 400;

        @Config.Name("Research Lab Progress Per Interval")
        @Config.Comment("Progress added by each loaded Research Lab per interval.")
        public int researchLabProgressPerInterval = 8;

        @Config.Name("Research Lab Progress Required")
        @Config.Comment("Progress required for a Research Lab to complete one available research entry.")
        public int researchLabProgressRequired = 160;

        @Config.Name("Research Lab Max Stored Samples")
        @Config.Comment("Maximum Parasite Tissue Samples a Research Lab can store for research completion costs.")
        public int researchLabMaxStoredSamples = 12;

        @Config.Name("Research Entries")
        @Config.Comment({
                "Research entries in the format id|category|name|description|pointReward|requiredResearchIds|sampleCost|supplyCost.",
                "Use comma-separated requiredResearchIds, or leave that field blank.",
                "Use 0 for sampleCost when the research should not consume Parasite Tissue Samples.",
                "Use 0 for supplyCost when the research should not consume stored supply points.",
                "Valid default categories are GENERAL, PARASITE_BIOLOGY, MILITARY_LOGISTICS, BASE_INFRASTRUCTURE, FIELD_MEDICINE, and SPECIAL_PROJECTS."
        })
        public String[] researchEntries = new String[] {
                "parasite_samples|PARASITE_BIOLOGY|Parasite Samples|Catalog recovered parasite tissue and establish basic containment procedures.|15||1|0",
                "field_communications|MILITARY_LOGISTICS|Field Communications|Coordinate survivor cells and local army response teams across infected territory.|20||0|20",
                "outpost_doctrine|BASE_INFRASTRUCTURE|Outpost Doctrine|Draft the first defensible outpost standards for later military construction.|35|field_communications|0|50",
                "special_division_training|SPECIAL_PROJECTS|Special Division Training|Train select operatives for anti-parasite rapid deployment and containment work.|100|parasite_samples,outpost_doctrine|4|120"
        };
    }

    public static final class Missions {
        @Config.Name("Mission Entries")
        @Config.Comment({
                "Mission definitions in the format id|name|description|objectiveType|requiredCount|pointReward|supplyReward|researchRewardIds.",
                "Use comma-separated researchRewardIds, or leave the final field blank.",
                "Current objective types: RECOVER_PARASITE_SAMPLE, DEFEND_OUTPOST, DISCOVER_STRUCTURE, DISCOVER_CHECKPOINT, DISCOVER_MAIN_BASE, and MANUAL.",
                "The first mission uses Parasite Tissue Samples as the tracked objective."
        })
        public String[] missionEntries = new String[] {
                "recover_parasite_sample|Recover Parasite Sample|Recover and catalog the first parasite tissue sample for the human resistance.|RECOVER_PARASITE_SAMPLE|1|30|15|parasite_samples"
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
        @Config.Comment("Ticks between passive point payouts from each loaded Field Command Post. 2400 ticks is about 2 minutes.")
        public int fieldCommandPostTickInterval = 2400;

        @Config.Name("Field Command Post Upgrade Requirements")
        @Config.Comment({
                "Upgrade requirements in the format targetLevel|requiredHumanPoints|requiredResearchIds|parasiteSampleCost|supplyCost|pointReward.",
                "targetLevel must be 2 through 5. Level names are fixed by the mod.",
                "Use comma-separated requiredResearchIds, or leave that field blank.",
                "Human points are required as progression, not spent. Parasite samples and supply points are consumed."
        })
        public String[] fieldCommandPostUpgradeRequirements = new String[] {
                "2|150|field_communications|1|30|15",
                "3|500|outpost_doctrine|2|80|30",
                "4|1200|outpost_doctrine,parasite_samples|5|160|60",
                "5|2600|outpost_doctrine,parasite_samples|10|320|100"
        };

        @Config.Name("Enable Outpost Defender Spawning")
        @Config.Comment("Allows loaded Field Command Posts to act as outpost anchors that spawn limited human defenders.")
        public boolean enableOutpostDefenderSpawning = true;

        @Config.Name("Outpost Max Defenders")
        @Config.Comment("Maximum living defenders assigned to each loaded outpost anchor.")
        public int outpostMaxDefenders = 2;

        @Config.Name("Outpost Defender Spawn Interval")
        @Config.Comment("Ticks between defender spawn attempts per loaded outpost anchor. 3600 ticks is about 3 minutes.")
        public int outpostDefenderSpawnInterval = 3600;

        @Config.Name("Outpost Defender Patrol Radius")
        @Config.Comment("Radius assigned defenders try to stay within around their outpost anchor.")
        public int outpostDefenderPatrolRadius = 16;

        @Config.Name("Outpost Defender Spawn Search Radius")
        @Config.Comment("Horizontal radius around the outpost anchor used to find a safe defender spawn position.")
        public int outpostDefenderSpawnSearchRadius = 4;
    }

    public static final class Supply {
        @Config.Name("Supply Crate Value")
        @Config.Comment({
                "Supply points provided by a Supply Crate.",
                "Claiming a Supply Crate block adds this to global supplies.",
                "Depositing a Supply Crate into a building adds this to that building's local stockpile."
        })
        public int supplyCrateValue = 8;

        @Config.Name("Command Post Max Stored Supplies")
        @Config.Comment("Maximum local supplies a loaded Field Command Post can store.")
        public int commandPostMaxStoredSupplies = 48;

        @Config.Name("Research Lab Max Stored Supplies")
        @Config.Comment("Maximum local supplies a Research Lab can store.")
        public int researchLabMaxStoredSupplies = 24;

        @Config.Name("Enable Command Post Supply Generation")
        @Config.Comment("Allows loaded Field Command Posts to generate local supply stockpile points over time.")
        public boolean enableCommandPostSupplyGeneration = true;

        @Config.Name("Command Post Supplies Per Interval")
        @Config.Comment("Local supply points generated by each loaded Field Command Post per interval.")
        public int commandPostSuppliesPerInterval = 1;

        @Config.Name("Command Post Supply Tick Interval")
        @Config.Comment("Ticks between passive supply generation from each loaded Field Command Post. 3600 ticks is about 3 minutes.")
        public int commandPostSupplyTickInterval = 3600;

        @Config.Name("Enable GUI Supply Transfers")
        @Config.Comment({
                "Allows building GUIs to move supplies between local building storage and the saved global supply pool.",
                "Transfers are server-authoritative and only touch the open tile entity; no global scans are used."
        })
        public boolean enableGuiSupplyTransfers = true;

        @Config.Name("GUI Supply Transfer Amount")
        @Config.Comment("Supply points moved by each Import or Export button press in building GUIs.")
        public int guiSupplyTransferAmount = 10;

        @Config.Name("Enable Command Post Logistics Route")
        @Config.Comment({
                "Enables the first logistics route placeholder for loaded Field Command Posts.",
                "When enabled, each loaded command post can periodically export local supplies into the saved global supply pool.",
                "This is tile-local and server-side; it does not scan the world for routes or spawn convoy entities yet."
        })
        public boolean enableCommandPostLogisticsRoute = true;

        @Config.Name("Command Post Logistics Route Interval")
        @Config.Comment("Ticks between logistics route export checks for each loaded Field Command Post.")
        public int commandPostLogisticsRouteInterval = 3600;

        @Config.Name("Command Post Logistics Route Transfer Amount")
        @Config.Comment("Local supplies exported to global supplies by each successful command post logistics route check.")
        public int commandPostLogisticsRouteTransferAmount = 4;

        @Config.Name("Command Post Logistics Route Minimum Local Supplies")
        @Config.Comment("Minimum local supplies a Field Command Post must store before its logistics route can export supplies.")
        public int commandPostLogisticsRouteMinimumLocalSupplies = 12;
    }

    public static final class Equipment {
        @Config.Name("Army Armour Durability")
        @Config.Comment("Base durability multiplier for the Army armour material.")
        public int armyArmorDurability = 18;

        @Config.Name("Army Armour Reductions")
        @Config.Comment("Damage reduction values for Army armour in boots, leggings, chestplate, helmet order.")
        public int[] armyArmorReductions = new int[] {
                2,
                5,
                6,
                2
        };

        @Config.Name("Army Armour Enchantability")
        @Config.Comment("Enchantability for the Army armour material.")
        public int armyArmorEnchantability = 9;

        @Config.Name("Army Armour Toughness")
        @Config.Comment("Armour toughness for the Army armour material.")
        public float armyArmorToughness = 0.0F;

        @Config.Name("Elite Armour Durability")
        @Config.Comment("Base durability multiplier for the Elite armour material.")
        public int eliteArmorDurability = 28;

        @Config.Name("Elite Armour Reductions")
        @Config.Comment("Damage reduction values for Elite armour in boots, leggings, chestplate, helmet order.")
        public int[] eliteArmorReductions = new int[] {
                3,
                6,
                8,
                3
        };

        @Config.Name("Elite Armour Enchantability")
        @Config.Comment("Enchantability for the Elite armour material.")
        public int eliteArmorEnchantability = 10;

        @Config.Name("Elite Armour Toughness")
        @Config.Comment("Armour toughness for the Elite armour material.")
        public float eliteArmorToughness = 1.0F;

        @Config.Name("Special Division Armour Durability")
        @Config.Comment("Base durability multiplier for the Special Division armour material.")
        public int specialDivisionArmorDurability = 36;

        @Config.Name("Special Division Armour Reductions")
        @Config.Comment("Damage reduction values for Special Division armour in boots, leggings, chestplate, helmet order.")
        public int[] specialDivisionArmorReductions = new int[] {
                3,
                6,
                8,
                3
        };

        @Config.Name("Special Division Armour Enchantability")
        @Config.Comment("Enchantability for the Special Division armour material.")
        public int specialDivisionArmorEnchantability = 14;

        @Config.Name("Special Division Armour Toughness")
        @Config.Comment("Armour toughness for the Special Division armour material.")
        public float specialDivisionArmorToughness = 2.0F;

        @Config.Name("Anti-Parasite Blade Harvest Level")
        @Config.Comment("Tool harvest level used by the basic anti-parasite melee weapon.")
        public int antiParasiteBladeHarvestLevel = 2;

        @Config.Name("Anti-Parasite Blade Max Uses")
        @Config.Comment("Durability for the basic anti-parasite melee weapon.")
        public int antiParasiteBladeMaxUses = 320;

        @Config.Name("Anti-Parasite Blade Efficiency")
        @Config.Comment("Tool efficiency for the basic anti-parasite melee weapon.")
        public float antiParasiteBladeEfficiency = 6.0F;

        @Config.Name("Anti-Parasite Blade Attack Damage")
        @Config.Comment("Tool material attack damage. Minecraft swords add their normal sword bonus on top of this value.")
        public float antiParasiteBladeAttackDamage = 5.0F;

        @Config.Name("Anti-Parasite Blade Enchantability")
        @Config.Comment("Enchantability for the basic anti-parasite melee weapon.")
        public int antiParasiteBladeEnchantability = 12;

        @Config.Name("Army Equipment Required Stage")
        @Config.Comment("Minimum human stage required to use Army equipment.")
        public int armyEquipmentRequiredStage = 1;

        @Config.Name("Army Equipment Required Research")
        @Config.Comment("Optional research ID required to use Army equipment. Leave blank to disable this gate.")
        public String armyEquipmentRequiredResearch = "field_communications";

        @Config.Name("Elite Equipment Required Stage")
        @Config.Comment("Minimum human stage required to use Elite equipment.")
        public int eliteEquipmentRequiredStage = 3;

        @Config.Name("Elite Equipment Required Research")
        @Config.Comment("Optional research ID required to use Elite equipment. Leave blank to disable this gate.")
        public String eliteEquipmentRequiredResearch = "outpost_doctrine";

        @Config.Name("Special Division Equipment Required Stage")
        @Config.Comment("Minimum human stage required to use Special Division equipment.")
        public int specialDivisionEquipmentRequiredStage = 5;

        @Config.Name("Special Division Equipment Required Research")
        @Config.Comment("Optional research ID required to use Special Division equipment. Leave blank to disable this gate.")
        public String specialDivisionEquipmentRequiredResearch = "special_division_training";

        @Config.Name("Anti-Parasite Blade Required Stage")
        @Config.Comment("Minimum human stage required to use the Anti-Parasite Blade.")
        public int antiParasiteBladeRequiredStage = 2;

        @Config.Name("Anti-Parasite Blade Required Research")
        @Config.Comment("Optional research ID required to use the Anti-Parasite Blade. Leave blank to disable this gate.")
        public String antiParasiteBladeRequiredResearch = "parasite_samples";

        @Config.Name("Prototype Ranged Weapon Required Stage")
        @Config.Comment("Minimum human stage required to use the prototype ranged weapon.")
        public int prototypeRangedWeaponRequiredStage = 1;

        @Config.Name("Prototype Ranged Weapon Required Research")
        @Config.Comment("Optional research ID required to use the prototype ranged weapon. Leave blank to disable this gate.")
        public String prototypeRangedWeaponRequiredResearch = "field_communications";

        @Config.Name("Prototype Ranged Weapon Damage")
        @Config.Comment("Damage dealt by the prototype ranged weapon projectile to configured parasite/test enemies.")
        public float prototypeRangedWeaponDamage = 5.0F;

        @Config.Name("Prototype Ranged Weapon Parasite Damage Multiplier")
        @Config.Comment("Multiplier applied when the prototype projectile damages configured parasite/test enemies.")
        public float prototypeRangedWeaponParasiteDamageMultiplier = 1.15F;

        @Config.Name("Prototype Ranged Weapon Cooldown")
        @Config.Comment("Player cooldown in ticks after firing the prototype ranged weapon.")
        public int prototypeRangedWeaponCooldownTicks = 30;

        @Config.Name("Prototype Ranged Weapon Max Uses")
        @Config.Comment("Durability for the prototype ranged weapon.")
        public int prototypeRangedWeaponMaxUses = 384;

        @Config.Name("Prototype Ranged Weapon Velocity")
        @Config.Comment("Projectile velocity for the prototype ranged weapon.")
        public float prototypeRangedWeaponVelocity = 2.0F;

        @Config.Name("Prototype Ranged Weapon Inaccuracy")
        @Config.Comment("Projectile inaccuracy for the prototype ranged weapon. Lower values are more accurate.")
        public float prototypeRangedWeaponInaccuracy = 1.5F;

        @Config.Name("Prototype Projectile Hit Particles")
        @Config.Comment("Client-side particles spawned when the prototype projectile hits a configured target.")
        public int prototypeProjectileHitParticles = 8;

        @Config.Name("Enable Prototype Projectile Hit Sound")
        @Config.Comment("Plays a simple hit sound when the prototype projectile damages a configured target.")
        public boolean enablePrototypeProjectileHitSound = true;

        @Config.Name("Enable Army Rifleman Ranged Weapon")
        @Config.Comment("Allows Army Rifleman NPCs to use the prototype ranged weapon against configured parasite/test enemies.")
        public boolean enableArmyRiflemanRangedWeapon = true;

        @Config.Name("Army Rifleman Ranged Attack Interval")
        @Config.Comment("Ticks between Army Rifleman ranged attacks.")
        public int armyRiflemanRangedAttackInterval = 50;

        @Config.Name("Army Rifleman Ranged Attack Range")
        @Config.Comment("Maximum ranged attack distance for Army Riflemen.")
        public float armyRiflemanRangedAttackRange = 16.0F;
    }

    public static final class HumanNpcs {
        @Config.Name("Enable Human Unit Parasite Targeting")
        @Config.Comment("Allows human combat units to attack configured parasite/test entity registry IDs.")
        public boolean enableHumanUnitParasiteTargeting = true;

        @Config.Name("Human Unit Target Entity IDs")
        @Config.Comment({
                "Entity registry IDs human combat units are allowed to target.",
                "Use verified parasite registry IDs here. The default minecraft:zombie entry is only a safe test value.",
                "Malformed entries and empty registry IDs are skipped safely.",
                "Players and Stand and Hold human NPCs are always ignored by default.",
                "SRP mappings can also allow targeting when their humanTarget field is true and SRP is loaded."
        })
        public String[] humanUnitTargetEntityIds = new String[] {
                "minecraft:zombie"
        };

        @Config.Name("Human Unit Stats")
        @Config.Comment({
                "Human unit tier stats in the format unitId|health|damage|requiredHumanStage.",
                "Valid unit IDs: survivor_defender, army_rifleman, heavy_soldier, elite_soldier, super_elite_soldier, special_parasite_division_operative.",
                "Required human stage gates spawning for that unit tier. Special Parasite Division Operatives are always clamped to at least Stage 5."
        })
        public String[] humanUnitStats = new String[] {
                "survivor_defender|16|2|0",
                "army_rifleman|20|4|1",
                "heavy_soldier|26|5|2",
                "elite_soldier|34|7|3",
                "super_elite_soldier|44|10|4",
                "special_parasite_division_operative|68|16|5"
        };

        @Config.Name("Enable Special Parasite Division Research Gate")
        @Config.Comment("Requires configured research before Special Parasite Division Operatives can spawn or deploy.")
        public boolean enableSpecialParasiteDivisionResearchGate = true;

        @Config.Name("Special Parasite Division Training Research ID")
        @Config.Comment("Research ID required to unlock Special Parasite Division Operatives.")
        public String specialParasiteDivisionTrainingResearchId = "special_division_training";

        @Config.Name("Enable Special Parasite Division Deployments")
        @Config.Comment("Allows active Main Bases to rarely deploy Special Parasite Division Operatives.")
        public boolean enableSpecialParasiteDivisionDeployments = true;

        @Config.Name("Special Parasite Division Deployment Interval")
        @Config.Comment("Ticks between Special Parasite Division deployment checks per loaded active Main Base.")
        public int specialParasiteDivisionDeploymentInterval = 12000;

        @Config.Name("Special Parasite Division Deployment Chance")
        @Config.Comment("One chance in this many deployment intervals to deploy an operative from an active Main Base.")
        public int specialParasiteDivisionDeploymentChance = 4;

        @Config.Name("Special Parasite Division Max Operatives Per Main Base")
        @Config.Comment("Maximum living Special Parasite Division Operatives assigned to each active Main Base.")
        public int specialParasiteDivisionMaxOperativesPerMainBase = 2;

        @Config.Name("Special Parasite Division Deployment Radius")
        @Config.Comment("Horizontal radius around a Main Base command post used to find deployment positions.")
        public int specialParasiteDivisionDeploymentRadius = 8;

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

    public static final class WorldGeneration {
        @Config.Name("Enable Army Checkpoint Generation")
        @Config.Comment("Allows Small Army Checkpoints to generate during terrain generation.")
        public boolean enableArmyCheckpointGeneration = true;

        @Config.Name("Army Checkpoint Spawn Chance")
        @Config.Comment("One chance in this many chunks to try generating a Small Army Checkpoint. Higher values are rarer.")
        public int armyCheckpointSpawnChance = 180;

        @Config.Name("Army Checkpoint Width")
        @Config.Comment("Checkpoint width in blocks. Values are clamped to a safe in-chunk range.")
        public int armyCheckpointWidth = 9;

        @Config.Name("Army Checkpoint Depth")
        @Config.Comment("Checkpoint depth in blocks. Values are clamped to a safe in-chunk range.")
        public int armyCheckpointDepth = 9;

        @Config.Name("Army Checkpoint Wall Height")
        @Config.Comment("Low perimeter wall height for generated checkpoints.")
        public int armyCheckpointWallHeight = 2;

        @Config.Name("Army Checkpoint Max Terrain Height Difference")
        @Config.Comment("Maximum height difference allowed across the checkpoint footprint before generation is skipped.")
        public int armyCheckpointMaxTerrainHeightDifference = 2;

        @Config.Name("Army Checkpoint Allowed Dimensions")
        @Config.Comment("Dimension IDs where Small Army Checkpoints can generate. Default is overworld only.")
        public int[] armyCheckpointAllowedDimensions = new int[] {
                0
        };

        @Config.Name("Enable Main Base Generation")
        @Config.Comment("Allows rare Main Base foundations to generate during terrain generation.")
        public boolean enableMainBaseGeneration = true;

        @Config.Name("Main Base Spawn Chance")
        @Config.Comment("One chance in this many chunks to try generating a Main Base. Higher values are rarer.")
        public int mainBaseSpawnChance = 3600;

        @Config.Name("Main Base Width")
        @Config.Comment("Main Base width in blocks. Values are clamped to a safe in-chunk range.")
        public int mainBaseWidth = 15;

        @Config.Name("Main Base Depth")
        @Config.Comment("Main Base depth in blocks. Values are clamped to a safe in-chunk range.")
        public int mainBaseDepth = 15;

        @Config.Name("Main Base Wall Height")
        @Config.Comment("Perimeter wall height for generated Main Bases.")
        public int mainBaseWallHeight = 3;

        @Config.Name("Main Base Max Terrain Height Difference")
        @Config.Comment("Maximum height difference allowed across the Main Base footprint before generation is skipped.")
        public int mainBaseMaxTerrainHeightDifference = 2;

        @Config.Name("Main Base Activation Stage")
        @Config.Comment("Minimum human stage required for a generated Main Base to fully activate. Stage 5 is Special Parasite Division.")
        public int mainBaseActivationStage = 5;

        @Config.Name("Main Base Initial Defenders")
        @Config.Comment("Number of defenders spawned on Main Base defender pads when the base generates while activated.")
        public int mainBaseInitialDefenders = 3;

        @Config.Name("Main Base Defender Patrol Radius")
        @Config.Comment("Patrol radius assigned to defenders spawned by activated Main Bases.")
        public int mainBaseDefenderPatrolRadius = 24;

        @Config.Name("Main Base Allowed Dimensions")
        @Config.Comment("Dimension IDs where Main Bases can generate. Default is overworld only.")
        public int[] mainBaseAllowedDimensions = new int[] {
                0
        };
    }

    public static final class DynamicEvents {
        @Config.Name("Enable Dynamic Events")
        @Config.Comment("Master toggle for Stand and Hold dynamic events and raid debug commands.")
        public boolean enableDynamicEvents = true;

        @Config.Name("Enable Natural Dynamic Events")
        @Config.Comment("Allows loaded Field Command Posts to periodically roll for natural outpost attacks or reinforcement events.")
        public boolean enableNaturalDynamicEvents = true;

        @Config.Name("Dynamic Event Interval")
        @Config.Comment("Ticks between natural dynamic event rolls per loaded Field Command Post. 12000 ticks is about 10 minutes.")
        public int dynamicEventIntervalTicks = 12000;

        @Config.Name("Dynamic Event Chance")
        @Config.Comment("One chance in this many intervals to start a natural event. Higher values are rarer.")
        public int dynamicEventChance = 5;

        @Config.Name("Outpost Attack Weight")
        @Config.Comment("Relative weight for natural outpost attack events.")
        public int outpostAttackWeight = 3;

        @Config.Name("Human Reinforcement Weight")
        @Config.Comment("Relative weight for natural human reinforcement events.")
        public int humanReinforcementWeight = 1;

        @Config.Name("Dynamic Event Spawn Radius")
        @Config.Comment("Horizontal radius around the event anchor used to find safe spawn positions.")
        public int dynamicEventSpawnRadius = 12;

        @Config.Name("Enable Dynamic Event Warnings")
        @Config.Comment("Sends a simple chat warning to nearby players when a natural dynamic event starts.")
        public boolean enableDynamicEventWarnings = true;

        @Config.Name("Dynamic Event Warning Radius")
        @Config.Comment("Radius around a command post where players receive natural event warning messages.")
        public int dynamicEventWarningRadius = 64;

        @Config.Name("Outpost Attack Entity IDs")
        @Config.Comment({
                "Entity registry IDs used as attackers for outpost attack events.",
                "The default minecraft:zombie entry is a safe test value for early development.",
                "Use verified parasite registry IDs here once compatibility mappings are confirmed."
        })
        public String[] outpostAttackEntityIds = new String[] {
                "minecraft:zombie"
        };

        @Config.Name("Outpost Attack Base Count")
        @Config.Comment("Base attacker count for an outpost attack before stage scaling.")
        public int outpostAttackBaseCount = 1;

        @Config.Name("Outpost Attackers Per Stage")
        @Config.Comment("Additional outpost attackers added for each current human stage.")
        public int outpostAttackersPerStage = 1;

        @Config.Name("Outpost Attack Max Count")
        @Config.Comment("Maximum attackers spawned by one outpost attack event. Use 0 for no cap.")
        public int outpostAttackMaxCount = 6;

        @Config.Name("Human Reinforcement Base Count")
        @Config.Comment("Base reinforcement count before stage scaling.")
        public int humanReinforcementBaseCount = 1;

        @Config.Name("Human Reinforcements Per Stage")
        @Config.Comment("Additional reinforcements added for each current human stage.")
        public int humanReinforcementsPerStage = 0;

        @Config.Name("Human Reinforcement Max Count")
        @Config.Comment("Maximum human units spawned by one reinforcement event. Use 0 for no cap.")
        public int humanReinforcementMaxCount = 2;

        @Config.Name("Human Reinforcement Patrol Radius")
        @Config.Comment("Patrol radius assigned to units spawned by dynamic reinforcement events.")
        public int humanReinforcementPatrolRadius = 24;
    }

    public static final class ThreatResponse {
        @Config.Name("Enable Threat Tracking")
        @Config.Comment("Tracks regional threat from configured parasite deaths, human losses, and outpost attacks.")
        public boolean enableThreatTracking = true;

        @Config.Name("Enable Threat Reinforcements")
        @Config.Comment("Allows high-threat regions to trigger bounded human reinforcements from active Main Bases.")
        public boolean enableThreatReinforcements = true;

        @Config.Name("Threat Region Chunk Size")
        @Config.Comment("Threat region width/depth in chunks. Larger values merge more events into fewer records.")
        public int threatRegionChunkSize = 4;

        @Config.Name("Threat Level Thresholds")
        @Config.Comment("Threat score thresholds for Low, Guarded, High, and Critical levels.")
        public int[] threatLevelThresholds = new int[] {
                0,
                12,
                35,
                70
        };

        @Config.Name("Parasite Kill Threat Increase")
        @Config.Comment("Threat score added when a configured parasite/test entity dies in a region.")
        public int parasiteKillThreatIncrease = 2;

        @Config.Name("Human Loss Threat Increase")
        @Config.Comment("Threat score added when a Stand and Hold human unit dies in a region.")
        public int humanLossThreatIncrease = 10;

        @Config.Name("Outpost Attack Threat Increase")
        @Config.Comment("Threat score added when an assigned outpost defender is attacked by a configured parasite/test entity.")
        public int outpostAttackThreatIncrease = 6;

        @Config.Name("Outpost Attack Threat Cooldown")
        @Config.Comment("Ticks before repeated outpost attack threat can be counted again in the same region.")
        public int outpostAttackThreatCooldownTicks = 200;

        @Config.Name("Threat Reinforcement Threshold")
        @Config.Comment("Threat score required before automatic reinforcements can be considered.")
        public int threatReinforcementThreshold = 35;

        @Config.Name("Reinforcement Cooldown")
        @Config.Comment("Ticks between automatic reinforcement deployments per threat region.")
        public int reinforcementCooldownTicks = 18000;

        @Config.Name("Reinforcement Units Per Trigger")
        @Config.Comment("Maximum human units spawned when a threat region triggers reinforcements.")
        public int reinforcementUnitsPerTrigger = 1;

        @Config.Name("Max Reinforcements Per Threat Region")
        @Config.Comment("Maximum total reinforcement units a single threat region can receive. Use 0 for no cap.")
        public int maxReinforcementsPerThreatRegion = 4;

        @Config.Name("Reinforcement Spawn Radius")
        @Config.Comment("Horizontal radius around a threat region center used to find reinforcement spawn positions.")
        public int reinforcementSpawnRadius = 8;

        @Config.Name("Reinforcement Patrol Radius")
        @Config.Comment("Patrol radius assigned to threat-response reinforcements around their target high-threat region.")
        public int reinforcementPatrolRadius = 32;

        @Config.Name("Enable Threat Decay")
        @Config.Comment("Allows threat scores to decay when threat records are touched by events or commands.")
        public boolean enableThreatDecay = true;

        @Config.Name("Threat Decay Interval")
        @Config.Comment("Ticks between each opportunistic threat decay step for a region.")
        public int threatDecayIntervalTicks = 24000;

        @Config.Name("Threat Decay Amount")
        @Config.Comment("Threat score removed from a region each decay interval.")
        public int threatDecayAmount = 1;

        @Config.Name("Max Threat Score")
        @Config.Comment("Maximum stored threat score per region.")
        public int maxThreatScore = 100;

        @Config.Name("Max Threat Records")
        @Config.Comment("Maximum number of regional threat records persisted in the world save.")
        public int maxThreatRecords = 128;
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
