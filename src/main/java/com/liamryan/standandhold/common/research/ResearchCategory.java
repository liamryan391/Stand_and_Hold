package com.liamryan.standandhold.common.research;

import java.util.Locale;

public enum ResearchCategory {
    GENERAL("General"),
    PARASITE_BIOLOGY("Parasite Biology"),
    MILITARY_LOGISTICS("Military Logistics"),
    BASE_INFRASTRUCTURE("Base Infrastructure"),
    FIELD_MEDICINE("Field Medicine"),
    SPECIAL_PROJECTS("Special Projects");

    private final String displayName;

    ResearchCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCommandName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ResearchCategory fromConfigValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return GENERAL;
        }

        String normalized = normalize(value);
        for (ResearchCategory category : values()) {
            if (normalize(category.name()).equals(normalized)
                    || normalize(category.displayName).equals(normalized)
                    || normalize(category.getCommandName()).equals(normalized)) {
                return category;
            }
        }

        return GENERAL;
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
