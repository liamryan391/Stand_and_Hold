package com.liamryan.standandhold.common.progression;

// Numeric save levels follow this order; reordering existing stages requires a migration.
public enum HumanStage {
    SURVIVORS("survivors", "Survivors"),
    LOCAL_ARMY_RESPONSE("local_army_response", "Local Army Response"),
    ORGANISED_MILITARY("organised_military", "Organised Military"),
    ELITE_UNITS("elite_units", "Elite Units"),
    SUPER_ELITE_UNITS("super_elite_units", "Super Elite Units"),
    SPECIAL_PARASITE_DIVISION("special_parasite_division", "Special Parasite Division"),
    MAIN_BASE_COUNTER_OFFENSIVE("main_base_counter_offensive", "Main Base / Endgame Counter-Offensive");

    private final String id;
    private final String displayName;

    HumanStage(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int level() { return ordinal(); }
    public String id() { return id; }
    public String displayName() { return displayName; }

    public static HumanStage fromLevel(int level) {
        if (level < 0 || level >= values().length) {
            throw new IllegalArgumentException("Stage must be between 0 and 6.");
        }
        return values()[level];
    }
}
