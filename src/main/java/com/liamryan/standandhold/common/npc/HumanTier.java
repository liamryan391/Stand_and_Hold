package com.liamryan.standandhold.common.npc;

/** Recruitment priority is declaration order; saved entity IDs never change tier. */
public enum HumanTier {
    SURVIVOR_DEFENDER("survivor_defender", 20, 3, 0.27, 2, 0),
    ARMY_RIFLEMAN("army_rifleman", 30, 5, 0.28, 4, 1),
    HEAVY_SOLDIER("heavy_soldier", 44, 6, 0.24, 8, 2),
    ELITE_SOLDIER("elite_soldier", 36, 7, 0.30, 6, 3),
    SUPER_ELITE("super_elite", 52, 8, 0.25, 10, 4),
    SPECIAL_DIVISION("special_division_operative", 42, 7, 0.31, 7, 5);

    public final String id;
    public final double health, damage, speed, armor;
    public final int stage;
    HumanTier(String id, double health, double damage, double speed, double armor, int stage) {
        this.id = id; this.health = health; this.damage = damage; this.speed = speed; this.armor = armor; this.stage = stage;
    }
    public static HumanTier unlocked(int stage, int survivorStage, int armyStage) {
        if (stage < 0 || stage > 6) { throw new IllegalArgumentException("Invalid human stage."); }
        return stage >= armyStage ? ARMY_RIFLEMAN : stage >= survivorStage ? SURVIVOR_DEFENDER : null;
    }
    public boolean usesBlade() { return this == SURVIVOR_DEFENDER || this == HEAVY_SOLDIER || this == SUPER_ELITE; }
    public boolean usesPrototype() { return this == ARMY_RIFLEMAN || this == ELITE_SOLDIER || this == SPECIAL_DIVISION; }
    public boolean usesLauncherMotion() { return this == ARMY_RIFLEMAN || this == SPECIAL_DIVISION; }
    public boolean advanced() { return this == SUPER_ELITE || this == SPECIAL_DIVISION; }
    public static HumanTier unlocked(int stage, java.util.function.ToIntFunction<HumanTier> requiredStage,
            java.util.function.Predicate<HumanTier> researchReady) {
        if (stage < 0 || stage > 6) { throw new IllegalArgumentException("Invalid human stage"); }
        HumanTier result = null;
        for (HumanTier tier : values()) {
            if (stage >= requiredStage.applyAsInt(tier) && researchReady.test(tier)) { result = tier; }
        }
        return result;
    }
    public static boolean hasRoom(int reserved, int postCap, int nearby, int areaCap) {
        return reserved >= 0 && nearby >= 0 && reserved < postCap && nearby < areaCap;
    }
}
