package com.liamryan.standandhold.common.mission;

import java.util.Locale;

public enum MissionObjectiveType {
    MANUAL("manual", "Manual Objective"),
    RECOVER_PARASITE_SAMPLE("recover_parasite_sample", "Recover Parasite Sample"),
    ESTABLISH_FIELD_COMMAND("establish_field_command", "Establish Field Command"),
    STOCKPILE_SUPPLIES("stockpile_supplies", "Stockpile Supplies"),
    COMPLETE_RESEARCH("complete_research", "Complete Research"),
    REACH_HUMAN_STAGE("reach_human_stage", "Reach Human Stage"),
    DEFEND_OUTPOST("defend_outpost", "Defend Outpost"),
    DISCOVER_STRUCTURE("discover_structure", "Discover Structure"),
    DISCOVER_CHECKPOINT("discover_checkpoint", "Discover Checkpoint"),
    DISCOVER_MAIN_BASE("discover_main_base", "Discover Main Base");

    private final String id;
    private final String displayName;

    MissionObjectiveType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MissionObjectiveType fromConfigValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return MANUAL;
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        for (MissionObjectiveType type : values()) {
            if (type.id.equals(normalizedValue) || type.name().toLowerCase(Locale.ROOT).equals(normalizedValue)) {
                return type;
            }
        }
        return MANUAL;
    }
}
