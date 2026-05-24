package com.liamryan.standandhold.common.progression;

import java.util.Locale;

public enum HumanStage {
    SURVIVORS(0, "Survivors"),
    LOCAL_ARMY_RESPONSE(1, "Local Army Response"),
    ORGANISED_MILITARY(2, "Organised Military"),
    ELITE_UNITS(3, "Elite Units"),
    SUPER_ELITE_UNITS(4, "Super Elite Units"),
    SPECIAL_PARASITE_DIVISION(5, "Special Parasite Division"),
    MAIN_BASE_COUNTER_OFFENSIVE(6, "Main Base / Endgame Counter-Offensive");

    private final int id;
    private final String displayName;

    HumanStage(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCommandName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static HumanStage byId(int id) {
        for (HumanStage stage : values()) {
            if (stage.id == id) {
                return stage;
            }
        }
        return SURVIVORS;
    }

    public static HumanStage fromCommandArgument(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = normalize(value);
        for (HumanStage stage : values()) {
            if (Integer.toString(stage.id).equals(normalized)
                    || normalize(stage.name()).equals(normalized)
                    || normalize(stage.displayName).equals(normalized)
                    || normalize(stage.getCommandName()).equals(normalized)) {
                return stage;
            }
        }

        return null;
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_')
                .replace('/', '_');
    }
}
