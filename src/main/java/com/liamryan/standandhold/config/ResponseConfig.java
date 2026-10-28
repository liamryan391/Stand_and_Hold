package com.liamryan.standandhold.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ResponseConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue REGIONS = B.comment("Maximum saved 64x64-block threat regions. Full storage skips NEW regions without evicting history; admin reset frees a region. Existing records survive lower limits.").defineInRange("maxRegions", 256, 1, 1024);
    public static final ModConfigSpec.IntValue KILL_SCORE = B.comment("Threat score added for a mapped threat killed by a player/human; no reward mapping changes.").defineInRange("killScore", 2, 0, 100);
    public static final ModConfigSpec.IntValue LOSS_SCORE = B.comment("Score per human NPC death, including accidents.").defineInRange("humanLossScore", 8, 0, 100);
    public static final ModConfigSpec.IntValue ATTACK_SCORE = B.comment("Score per attack window on assigned defenders; max one window per region per 20 ticks.").defineInRange("outpostAttackScore", 4, 0, 100);
    public static final ModConfigSpec.IntValue DECAY_INTERVAL = B.comment("Game ticks per lazy decay period. Only events, commands and loaded posts touch records; no global tick scan. Unloaded time is accounted when next inspected.").defineInRange("decayIntervalTicks", 1200, 200, 72000);
    public static final ModConfigSpec.IntValue DECAY_AMOUNT = B.comment("Score removed per period; counters never decay. Zero disables score decay.").defineInRange("decayAmount", 2, 0, 1000);
    public static final ModConfigSpec.BooleanValue RAIDS = B.comment("Natural raids require a loaded operational post, nearby non-spectator player, threat threshold, chance and saved cooldown. Debug bypasses natural grace/chance/threshold/player, never actual event cooldowns, quotas or spawn safety.").define("naturalRaids", true);
    public static final ModConfigSpec.BooleanValue REINFORCEMENTS = B.comment("Natural reinforcements share existing deployment enable/stage/post/area quotas, not an extra defender budget.").define("naturalReinforcements", true);
    public static final ModConfigSpec.BooleanValue WARNINGS = B.comment("Notify nearby non-spectators once per successful raid/deployment. Debug failures only reply to the operator.").define("playerWarnings", true);
    public static final ModConfigSpec.IntValue CHECK_INTERVAL = B.comment("Loaded post ticks between bounded event checks; no offline catch-up.").defineInRange("checkIntervalTicks", 200, 100, 1200);
    public static final ModConfigSpec.IntValue GRACE = B.comment("Initial natural-event grace per post; persistent across reload. Does not delay the first admin event command; existing saved cooldowns are never erased.").defineInRange("initialGraceTicks", 1200, 200, 72000);
    public static final ModConfigSpec.IntValue CHANCE = B.comment("One-in-N natural raid chance per eligible loaded-post check.").defineInRange("raidChanceOneIn", 30, 1, 10000);
    public static final ModConfigSpec.IntValue RAID_THRESHOLD = B.comment("Minimum decayed local score for natural raids.").defineInRange("raidThreatThreshold", 10, 0, 1000000);
    public static final ModConfigSpec.IntValue REINFORCEMENT_THRESHOLD = B.comment("Minimum local/adjacent-region score for natural reinforcements.").defineInRange("reinforcementThreatThreshold", 20, 0, 1000000);
    public static final ModConfigSpec.IntValue RAID_COOLDOWN = B.comment("Minimum game ticks between successful raids at one post; saved. Debug respects this too.").defineInRange("raidCooldownTicks", 6000, 200, 72000);
    public static final ModConfigSpec.IntValue REINFORCEMENT_COOLDOWN = B.comment("Saved ticks between successful reinforcement deployments.").defineInRange("reinforcementCooldownTicks", 2400, 200, 72000);
    public static final ModConfigSpec.IntValue BACKOFF = B.comment("Saved delay after an eligible attempt cannot spawn; not retried every tick.").defineInRange("failedSpawnBackoffTicks", 400, 200, 72000);
    public static final ModConfigSpec.IntValue RAID_CAP = B.comment("Per-post raiders incl. unloaded UUID reservations. Stage count is min(cap, 1 + stage/2); only vanilla zombies in this foundation.").defineInRange("maxRaidersPerPost", 4, 1, 8);
    public static final ModConfigSpec.IntValue RAID_AREA_CAP = B.comment("Max living loaded Enemy/mapped mobs in a 32-block box, including ordinary monsters and nearby raids. At most 128 mobs inspected; dense/unsafe/unloaded sites refuse the event.").defineInRange("maxHostilesInArea", 8, 1, 32);
    public static final ModConfigSpec.IntValue RAID_LIFETIME = B.comment("Tagged raiders expire after these game ticks when loaded. Unloaded UUIDs remain reserved; absence never proves death. No force loading.").defineInRange("raiderLifetimeTicks", 2400, 200, 72000);
    public static final ModConfigSpec.IntValue PATROL_DURATION = B.comment("Reinforcement patrol duration in game ticks; target is clamped inside the existing home radius, no distant/unloaded pursuit. Combat has priority.").defineInRange("patrolDurationTicks", 1200, 200, 72000);
    public static final ModConfigSpec.BooleanValue SUPPORT = B.comment("Allow nearby non-spectator players to request one supply-funded reinforcement while looking at a loaded Command Post. Shares normal deployment quotas/cooldowns; no vehicle/aircraft or remote chunk loading.").define("supportCallIns", true);
    public static final ModConfigSpec.IntValue SUPPORT_COST = B.comment("Global supplies spent once on a successful call-in. Known cooldown/quota/terrain refusals spend nothing. All players share the world bank.").defineInRange("supportSupplyCost", 10, 1, 1000000);
    public static final ModConfigSpec.IntValue SUPPORT_STAGE = B.comment("Minimum human stage for player support requests; automatic and admin deployments retain their existing gates.").defineInRange("supportRequiredStage", 1, 0, 6);
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_RESEARCH = B.comment("Research ID required for player support, or empty for none. Missing/malformed IDs refuse safely; does not add a mod dependency.").define("supportRequiredResearch", "standandhold:field_communications");
    public static final ModConfigSpec SPEC = B.build();
    private ResponseConfig() { }
}
