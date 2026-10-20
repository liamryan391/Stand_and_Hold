package com.liamryan.standandhold.common.research;

import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;

public final class ResearchManager {
    private ResearchManager() { }

    public static String blockedReason(HumanWorldData data, ResearchEntry entry, long[] thresholds) {
        data.requireSupportedVersion();
        if (data.isResearchComplete(entry.id())) { return "completed"; }
        if (ProgressionRules.stageFor(data.points(), thresholds).level() < entry.requiredStage()) {
            return "requires human stage " + entry.requiredStage();
        }
        for (String prerequisite : entry.prerequisites()) {
            if (!data.isResearchComplete(prerequisite)) { return "requires research " + prerequisite; }
        }
        if (entry.sampleCost() > 0) { return "requires " + entry.sampleCost() + " physical samples; use a Research Lab (commands cannot spend local samples)"; }
        if (data.supplies() < entry.supplyCost()) { return "requires " + entry.supplyCost() + " supplies; stored " + data.supplies(); }
        return "ready";
    }

    public static boolean complete(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, String id) {
        ResearchEntry entry = catalog.research(id);
        if (data.isResearchComplete(entry.id())) { return false; }
        String blocked = blockedReason(data, entry, thresholds);
        if (!blocked.equals("ready")) { throw new IllegalArgumentException("Research blocked: " + blocked); }
        long supplies = FoundationAmounts.credit(FoundationAmounts.debit(data.supplies(), entry.supplyCost()), entry.reward().supplies());
        long points = FoundationAmounts.credit(data.points(), entry.reward().points());
        HumanStage stage = ProgressionRules.stageFor(points, thresholds);
        return data.completeResearch(entry.id(), points, stage, supplies);
    }

    public static boolean hasUnlock(HumanWorldData data, FoundationCatalog catalog, String id) {
        data.requireSupportedVersion();
        String unlock = FoundationCatalog.commandId(id);
        return catalog.research().values().stream()
                .anyMatch(entry -> entry.unlocks().contains(unlock) && data.isResearchComplete(entry.id()));
    }
}
