package com.liamryan.standandhold.config;

import com.liamryan.standandhold.common.equipment.EquipmentKind;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class EquipmentConfig {
    public static final boolean DEFAULT_PLAYER_GATES = false;
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue PLAYER_GATES = B.comment("By default players can use/equip found gear at any stage. Enable only for an opt-in strict player progression pack. NPC weapon/loadout gates always apply; this does not bypass cooldowns, durability or friendly-fire protection.").define("gatePlayerEquipment", DEFAULT_PLAYER_GATES);
    public static final class Gate {
        public final ModConfigSpec.IntValue stage;
        public final ModConfigSpec.ConfigValue<String> research;
        private Gate(EquipmentKind kind) {
            B.push(kind.id);
            stage = B.comment("Minimum world human stage for normal equip/use. Possession/crafting is not gated; creative/admin edits are test access.")
                    .defineInRange("requiredStage", kind.stage, 0, 6);
            research = B.comment("Required verified research ID, or empty for none. Undefined/malformed IDs block use safely; no external mod is required.")
                    .define("requiredResearch", kind.research);
            B.pop();
        }
    }
    public static final Gate BLADE = new Gate(EquipmentKind.BLADE), ARMY = new Gate(EquipmentKind.ARMY),
            ELITE = new Gate(EquipmentKind.ELITE), PROTOTYPE = new Gate(EquipmentKind.PROTOTYPE);
    public static final ModConfigSpec.DoubleValue BLADE_DAMAGE = B.comment("Blade total base attack damage for a player with ordinary base attack 1; re-equip after edits.")
            .defineInRange("bladeDamage", 6.0, 1, 64);
    public static final ModConfigSpec.DoubleValue THREAT_MULTIPLIER = B.comment("Blade damage multiplier against existing configured threat mappings only; players/human NPCs are protected.")
            .defineInRange("bladeThreatMultiplier", 1.5, 1, 4);
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> ARMY_DEFENSE = B.comment("Helmet/chestplate/leggings/boots protection. Re-equip after edits. Static durability is not changed.")
            .defineList("armyDefense", List.of(2, 6, 5, 2), () -> 0, value -> value instanceof Integer n && n >= 0 && n <= 10);
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> ELITE_DEFENSE = B.comment("Helmet/chestplate/leggings/boots protection. Exactly four values; malformed lists use safe defaults.")
            .defineList("eliteDefense", List.of(3, 7, 6, 3), () -> 0, value -> value instanceof Integer n && n >= 0 && n <= 10);
    public static final ModConfigSpec.DoubleValue ARMY_TOUGHNESS = B.comment("Toughness per Army armour piece; re-equip after edits.").defineInRange("armyToughness", 0.0, 0, 4);
    public static final ModConfigSpec.DoubleValue ELITE_TOUGHNESS = B.comment("Toughness per Elite armour piece; re-equip after edits.").defineInRange("eliteToughness", 1.0, 0, 4);
    public static final ModConfigSpec.DoubleValue DAMAGE = B.comment("Prototype damage before vanilla mitigation. Only Enemy-marked hostile mobs or mapped threats can be damaged; no extra kill rewards are implied.").defineInRange("prototypeDamage", 6.0, 0, 64);
    public static final ModConfigSpec.IntValue COOLDOWN = B.comment("Shared player cooldown across both hands/all copies, in ticks. No ammo/reload system.").defineInRange("prototypeCooldownTicks", 20, 10, 200);
    public static final ModConfigSpec.IntValue LIFETIME = B.comment("Maximum bolt flight ticks; age/damage/lifetime persist and never reset on reload. No chunk tickets.").defineInRange("projectileLifetimeTicks", 30, 10, 100);
    public static final ModConfigSpec.DoubleValue SPEED = B.comment("Bolt speed in blocks/tick. Unavailable chunks/world border end flight safely.").defineInRange("projectileSpeed", 1.6, 0.5, 3);
    public static final ModConfigSpec.BooleanValue NPC_ENABLED = B.comment("Unlocked Army Riflemen may equip a non-dropping prototype in an empty hand and use bounded vanilla ranged AI; no new units/deployment changes.").define("riflemanPrototypeEnabled", true);
    public static final ModConfigSpec.IntValue NPC_INTERVAL = B.comment("Minimum Rifleman ticks between shots; separate from the player's cooldown.").defineInRange("riflemanShotIntervalTicks", 40, 20, 200);
    public static final ModConfigSpec SPEC = B.build();
    private EquipmentConfig() { }
    public static Gate gate(EquipmentKind kind) { return switch (kind) { case BLADE -> BLADE; case ARMY -> ARMY; case ELITE -> ELITE; case PROTOTYPE -> PROTOTYPE; }; }
    public static <T> T value(ModConfigSpec.ConfigValue<T> value) { return SPEC.isLoaded() ? value.get() : value.getDefault(); }
    public static int defense(EquipmentKind kind, int piece) {
        var values = value(kind == EquipmentKind.ELITE ? ELITE_DEFENSE : ARMY_DEFENSE);
        return values.size() == 4 ? values.get(piece) : (kind == EquipmentKind.ELITE ? new int[]{3, 7, 6, 3} : new int[]{2, 6, 5, 2})[piece];
    }
}
