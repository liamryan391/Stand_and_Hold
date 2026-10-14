package com.liamryan.standandhold.common.research;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import java.util.List;

public record ResearchEntry(String id, String title, int requiredStage, List<String> prerequisites,
                            long supplyCost, int sampleCost, FoundationReward reward, List<String> unlocks) {
    public ResearchEntry {
        FoundationCatalog.requireId(id);
        FoundationCatalog.requireTitle(title);
        prerequisites = List.copyOf(prerequisites);
        unlocks = List.copyOf(unlocks);
        if (requiredStage < 0 || requiredStage > 6 || supplyCost < 0 || sampleCost < 0 || sampleCost > 1_000_000) {
            throw new IllegalArgumentException("Invalid research requirements for " + id);
        }
        prerequisites.forEach(FoundationCatalog::requireId);
        unlocks.forEach(FoundationCatalog::requireId);
        if (reward == null) { throw new IllegalArgumentException("Missing research reward."); }
    }
}
