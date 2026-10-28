package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.npc.HumanTier;
import com.liamryan.standandhold.common.npc.DefenderCapacity;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class NpcConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final class Stats {
        public final ModConfigSpec.DoubleValue health, damage, speed, armor;
        public final ModConfigSpec.IntValue stage;
        private Stats(HumanTier tier) {
            B.push(tier.id);
            health = B.comment("Maximum health for newly spawned/reloaded NPCs. Existing health is clamped, not healed on reload.")
                    .defineInRange("health", tier.health, 1, 1024);
            damage = B.comment("Base melee damage. Rifleman's optional prototype ranged damage/unlock/interval is in standandhold-equipment.toml.")
                    .defineInRange("damage", tier.damage, 0, 2048);
            speed = B.comment("Movement speed for newly spawned/reloaded NPCs.").defineInRange("speed", tier.speed, 0.05, 0.6);
            armor = B.comment("Intrinsic armor points; no equipment drops.").defineInRange("armor", tier.armor, 0, 30);
            stage = B.comment("Minimum stage for automatic deployment only. Creative test eggs and admin /summon bypass it.")
                    .defineInRange("requiredStage", tier.stage, 0, 6);
            B.pop();
        }
    }
    public static final Stats SURVIVOR = new Stats(HumanTier.SURVIVOR_DEFENDER);
    public static final Stats ARMY = new Stats(HumanTier.ARMY_RIFLEMAN);
    public static final ModConfigSpec.BooleanValue HOSTILE_TARGETS = B.comment("Human defenders also target vanilla Enemy-marked hostile mobs, not only configured infection/test IDs. Does not grant kill rewards or samples to those mobs. Normally neutral Enemy types such as Endermen are included; disable to use mappings only.")
            .define("targetHostileMobs", true);
    public static final ModConfigSpec.BooleanValue DEPLOY = B.comment("Loaded command posts may deploy one defender per interval. No natural biome spawning or offline catch-up.")
            .define("deploymentEnabled", true);
    public static final ModConfigSpec.IntValue INTERVAL = B.comment("Loaded ticks per deployment attempt; timers restart after unload. At most one successful defender per attempt.")
            .defineInRange("deploymentIntervalTicks", 1200, 200, 72000);
    public static final ModConfigSpec.IntValue POST_CAP = B.comment("Absolute ceiling on saved reservations per post, including unloaded defenders. 0 disables ALL deployment paths; lowering caps never deletes units.",
                    "Effective cap is min(this ceiling, defendersByPostLevel entry, maxDefendersInArea, 16). Existing explicit values (including the old default 3) remain authoritative.",
                    "Fresh configs default to 16 to allow level growth. Existing servers may deliberately raise their ceiling to 8 to opt into the default profile; no config reset needed.")
            .defineInRange("maxDefendersPerPost", 16, 0, DefenderCapacity.ROSTER_LIMIT);
    public static final ModConfigSpec.ConfigValue<List<? extends Number>> POST_LEVELS = B.comment("Capacity targets for saved Command Post levels 1-5, independent of world human stage. Exactly five non-decreasing whole numbers from 0 to 16; 0 disables that level.",
                    "Invalid profiles reset to [3, 5, 6, 7, 8]; the absolute ceiling and shared-area cap still apply. Upgrades do not instantly spawn units or enlarge structures.")
            .define("defendersByPostLevel", DefenderCapacity.DEFAULT_LEVELS, DefenderCapacity::validProfile);
    public static final ModConfigSpec.IntValue AREA_CAP = B.comment("Maximum loaded human NPCs of either tier near a post, including test eggs and other posts' defenders.")
            .defineInRange("maxDefendersInArea", 8, 1, 64);
    public static final ModConfigSpec.IntValue AREA_RADIUS = B.comment("Radius of the bounded loaded-entity quota query. Does not load chunks.")
            .defineInRange("areaRadius", 48, 32, 64);
    public static final ModConfigSpec.IntValue HOME_RADIUS = B.comment("Vanilla home/pursuit radius for deployed defenders. No teleporting or chunk tickets.")
            .defineInRange("homeRadius", 16, 8, 24);
    public static final ModConfigSpec.IntValue SPAWN_RADIUS = B.comment("Candidate spawn radius around each post, with up to 8 attempts and 5 vertical positions. Unsafe/unloaded terrain is skipped.")
            .defineInRange("spawnRadius", 6, 2, 8);
    public static final ModConfigSpec.IntValue TARGET_INTERVAL = B.comment("Minimum ticks between target searches per NPC; only the local follow-range box is queried.")
            .defineInRange("targetSearchIntervalTicks", 20, 20, 200);
    public static final ModConfigSpec SPEC = B.build();
    private NpcConfig() { }
    public static Stats stats(HumanTier tier) { return tier == HumanTier.ARMY_RIFLEMAN ? ARMY : SURVIVOR; }
    public static int capacity(int level) { return DefenderCapacity.effective(level, POST_LEVELS.get(), POST_CAP.get(), AREA_CAP.get()); }
}
