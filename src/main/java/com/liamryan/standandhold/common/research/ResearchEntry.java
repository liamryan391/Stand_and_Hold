package com.liamryan.standandhold.common.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ResearchEntry {
    private final String id;
    private final ResearchCategory category;
    private final String displayName;
    private final String description;
    private final int completionPointReward;
    private final List<String> requiredResearchIds;
    private final int parasiteSampleCost;

    public ResearchEntry(String id, ResearchCategory category, String displayName, String description, int completionPointReward, List<String> requiredResearchIds, int parasiteSampleCost) {
        this.id = normalizeId(id);
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("Research id cannot be empty.");
        }

        this.category = category == null ? ResearchCategory.GENERAL : category;
        this.displayName = isBlank(displayName) ? this.id : displayName.trim();
        this.description = isBlank(description) ? "" : description.trim();
        this.completionPointReward = Math.max(0, completionPointReward);
        this.requiredResearchIds = normalizeRequiredResearchIds(requiredResearchIds);
        this.parasiteSampleCost = Math.max(0, parasiteSampleCost);
    }

    public String getId() {
        return id;
    }

    public ResearchCategory getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCompletionPointReward() {
        return completionPointReward;
    }

    public List<String> getRequiredResearchIds() {
        return requiredResearchIds;
    }

    public int getParasiteSampleCost() {
        return parasiteSampleCost;
    }

    public boolean hasRequirements() {
        return !requiredResearchIds.isEmpty();
    }

    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static List<String> normalizeRequiredResearchIds(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalizedIds = new ArrayList<String>();
        for (String rawId : rawIds) {
            String normalizedId = normalizeId(rawId);
            if (!normalizedId.isEmpty() && !normalizedIds.contains(normalizedId)) {
                normalizedIds.add(normalizedId);
            }
        }

        return Collections.unmodifiableList(normalizedIds);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
