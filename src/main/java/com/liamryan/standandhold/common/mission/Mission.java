package com.liamryan.standandhold.common.mission;

import com.liamryan.standandhold.common.research.ResearchEntry;

import java.util.Collections;
import java.util.List;

public final class Mission {
    private final String id;
    private final String displayName;
    private final String description;
    private final MissionObjectiveType objectiveType;
    private final int requiredCount;
    private final int pointReward;
    private final int supplyReward;
    private final List<String> researchRewardIds;

    public Mission(String id, String displayName, String description, MissionObjectiveType objectiveType, int requiredCount, int pointReward, int supplyReward, List<String> researchRewardIds) {
        String normalizedId = normalizeId(id);
        if (normalizedId.isEmpty()) {
            throw new IllegalArgumentException("Mission id cannot be empty.");
        }

        this.id = normalizedId;
        this.displayName = displayName == null || displayName.trim().isEmpty() ? normalizedId : displayName.trim();
        this.description = description == null ? "" : description.trim();
        this.objectiveType = objectiveType == null ? MissionObjectiveType.MANUAL : objectiveType;
        this.requiredCount = Math.max(1, requiredCount);
        this.pointReward = Math.max(0, pointReward);
        this.supplyReward = Math.max(0, supplyReward);
        this.researchRewardIds = researchRewardIds == null ? Collections.<String>emptyList() : Collections.unmodifiableList(researchRewardIds);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public MissionObjectiveType getObjectiveType() {
        return objectiveType;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public int getPointReward() {
        return pointReward;
    }

    public int getSupplyReward() {
        return supplyReward;
    }

    public List<String> getResearchRewardIds() {
        return researchRewardIds;
    }

    public boolean hasResearchRewards() {
        return !researchRewardIds.isEmpty();
    }

    public static String normalizeId(String id) {
        return ResearchEntry.normalizeId(id);
    }
}
