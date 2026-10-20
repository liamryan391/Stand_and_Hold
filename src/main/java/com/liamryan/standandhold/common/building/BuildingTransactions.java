package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.world.HumanWorldData;

/** Validate and construct the full result before any live building is changed. Server thread only. */
public final class BuildingTransactions {
    private BuildingTransactions() { }
    public record Transfer(BuildingState local, long global) { }
    public record Upgrade(long points, long supplies, int samples, String research) {
        public Upgrade {
            if (points < 0 || supplies < 0 || samples < 0) { throw new IllegalArgumentException("Negative upgrade cost."); }
        }
    }

    public static Transfer transfer(BuildingState state, long global, long amount, long cap, boolean deposit) {
        if (amount <= 0 || cap < 1) { throw new IllegalArgumentException("Invalid transfer amount/capacity."); }
        if (deposit) {
            long local = FoundationAmounts.credit(state.supplies(), amount);
            if (local > cap) { throw new IllegalArgumentException("Local supply storage is full."); }
            return new Transfer(state.supplies(local), FoundationAmounts.debit(global, amount));
        }
        return new Transfer(state.supplies(FoundationAmounts.debit(state.supplies(), amount)), FoundationAmounts.credit(global, amount));
    }

    public static String upgradeBlocked(BuildingState state, HumanWorldData data, Upgrade cost) {
        data.requireSupportedVersion();
        if (state.level() == 5) { return "Maximum level"; }
        if (data.points() < cost.points()) { return "Requires " + cost.points() + " human points (not spent)"; }
        if (!cost.research().isEmpty() && !data.isResearchComplete(cost.research())) { return "Requires " + cost.research(); }
        if (state.supplies() < cost.supplies()) { return "Requires " + cost.supplies() + " local supplies"; }
        if (state.samples() < cost.samples()) { return "Requires " + cost.samples() + " local samples"; }
        return "Ready";
    }

    public static BuildingState upgrade(BuildingState state, HumanWorldData data, Upgrade cost) {
        String blocked = upgradeBlocked(state, data, cost);
        if (!blocked.equals("Ready")) { throw new IllegalArgumentException(blocked); }
        return new BuildingState(state.level() + 1, state.supplies() - cost.supplies(), state.samples() - cost.samples(), state.target(), state.work());
    }

    public static String researchBlocked(BuildingState state, HumanWorldData data, ResearchEntry entry, long[] thresholds) {
        data.requireSupportedVersion();
        if (data.isResearchComplete(entry.id())) { return "Completed"; }
        if (ProgressionRules.stageFor(data.points(), thresholds).level() < entry.requiredStage()) { return "Requires stage " + entry.requiredStage(); }
        for (String id : entry.prerequisites()) { if (!data.isResearchComplete(id)) { return "Requires " + id; } }
        if (state.supplies() < entry.supplyCost()) { return "Requires " + entry.supplyCost() + " local supplies"; }
        if (state.samples() < entry.sampleCost()) { return "Requires " + entry.sampleCost() + " local samples"; }
        return "Ready";
    }

    public static BuildingState finishResearch(BuildingState state, HumanWorldData data, ResearchEntry entry, long[] thresholds, int requiredWork) {
        String blocked = researchBlocked(state, data, entry, thresholds);
        if (!blocked.equals("Ready")) { throw new IllegalArgumentException(blocked); }
        if (!state.target().equals(entry.id()) || state.work() < requiredWork || requiredWork < 1) {
            throw new IllegalArgumentException("Research work is not complete.");
        }
        BuildingState next = new BuildingState(state.level(), state.supplies() - entry.supplyCost(),
                state.samples() - entry.sampleCost(), state.target(), state.work());
        long points = FoundationAmounts.credit(data.points(), entry.reward().points());
        long supplies = FoundationAmounts.credit(data.supplies(), entry.reward().supplies());
        data.completeResearch(entry.id(), points, ProgressionRules.stageFor(points, thresholds), supplies);
        return next;
    }
}
