package com.liamryan.standandhold.common.infrastructure;

import com.liamryan.standandhold.common.research.ResearchEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FieldCommandPostUpgradeRequirement {
    private final FieldCommandPostLevel targetLevel;
    private final int requiredHumanPoints;
    private final List<String> requiredResearchIds;
    private final int parasiteSampleCost;
    private final int supplyCost;
    private final int completionPointReward;

    public FieldCommandPostUpgradeRequirement(FieldCommandPostLevel targetLevel, int requiredHumanPoints, List<String> requiredResearchIds, int parasiteSampleCost, int supplyCost, int completionPointReward) {
        this.targetLevel = targetLevel == null ? FieldCommandPostLevel.FIELD_CAMP : targetLevel;
        this.requiredHumanPoints = Math.max(0, requiredHumanPoints);
        this.requiredResearchIds = normalizeResearchIds(requiredResearchIds);
        this.parasiteSampleCost = Math.max(0, parasiteSampleCost);
        this.supplyCost = Math.max(0, supplyCost);
        this.completionPointReward = Math.max(0, completionPointReward);
    }

    public FieldCommandPostLevel getTargetLevel() {
        return targetLevel;
    }

    public int getRequiredHumanPoints() {
        return requiredHumanPoints;
    }

    public List<String> getRequiredResearchIds() {
        return requiredResearchIds;
    }

    public int getParasiteSampleCost() {
        return parasiteSampleCost;
    }

    public int getSupplyCost() {
        return supplyCost;
    }

    public int getCompletionPointReward() {
        return completionPointReward;
    }

    private static List<String> normalizeResearchIds(List<String> rawResearchIds) {
        if (rawResearchIds == null || rawResearchIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalizedIds = new ArrayList<String>();
        for (String rawResearchId : rawResearchIds) {
            String normalizedId = ResearchEntry.normalizeId(rawResearchId);
            if (!normalizedId.isEmpty() && !normalizedIds.contains(normalizedId)) {
                normalizedIds.add(normalizedId);
            }
        }

        return Collections.unmodifiableList(normalizedIds);
    }
}
