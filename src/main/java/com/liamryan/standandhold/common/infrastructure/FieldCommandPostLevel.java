package com.liamryan.standandhold.common.infrastructure;

public enum FieldCommandPostLevel {
    FIELD_CAMP(1, "Field Camp"),
    REINFORCED_OUTPOST(2, "Reinforced Outpost"),
    MILITARY_OUTPOST(3, "Military Outpost"),
    FORTIFIED_BASE(4, "Fortified Base"),
    MAIN_BASE(5, "Main Base");

    private final int level;
    private final String displayName;

    FieldCommandPostLevel(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FieldCommandPostLevel getNextLevel() {
        return byLevel(level + 1);
    }

    public boolean isMaxLevel() {
        return this == MAIN_BASE;
    }

    public static FieldCommandPostLevel byLevel(int level) {
        for (FieldCommandPostLevel commandPostLevel : values()) {
            if (commandPostLevel.level == level) {
                return commandPostLevel;
            }
        }

        return level <= FIELD_CAMP.level ? FIELD_CAMP : MAIN_BASE;
    }
}
