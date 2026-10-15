package com.liamryan.standandhold.common.mission;

import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.ArrayList;
import java.util.List;

public final class MissionManager {
    private MissionManager() { }

    public static Update start(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, String id) {
        Mission mission = catalog.mission(id);
        var old = data.mission(mission.id());
        if (old.isPresent()) { return new Update(mission, old.get(), false); }
        requirePrerequisites(data, mission);
        return update(data, mission, thresholds, observed(data, mission, thresholds));
    }

    public static Update advance(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, String id, long amount) {
        if (amount <= 0) { throw new IllegalArgumentException("Mission progress amount must be positive."); }
        Mission mission = catalog.mission(id);
        MissionState state = data.mission(mission.id()).orElseThrow(() -> new IllegalArgumentException("Start this mission first: " + mission.id()));
        if (state.completed()) { return new Update(mission, state, false); }
        long progress = state.progress() >= mission.target() ? state.progress()
                : state.progress() + Math.min(amount, mission.target() - state.progress());
        return update(data, mission, thresholds, progress);
    }

    /** Bounded explicit refresh, never a tick: each mission transaction succeeds or fails independently. */
    public static List<String> refresh(HumanWorldData data, FoundationCatalog catalog, long[] thresholds) {
        List<String> messages = new ArrayList<>();
        for (Mission mission : catalog.missions().values()) {
            try {
                var state = data.mission(mission.id());
                if (state.isEmpty() || state.get().completed()) { continue; }
                Update update = update(data, mission, thresholds, observed(data, mission, thresholds));
                if (update.changed()) { messages.add(update.message()); }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                messages.add("Mission update blocked: " + mission.id() + " - " + exception.getMessage());
            }
        }
        return List.copyOf(messages);
    }

    private static Update update(HumanWorldData data, Mission mission, long[] thresholds, long progress) {
        MissionState old = data.mission(mission.id()).orElse(new MissionState(0, false));
        if (old.completed()) { return new Update(mission, old, false); }
        long next = Math.max(old.progress(), Math.min(progress, mission.target()));
        boolean completed = next >= mission.target();
        long points = data.points();
        long supplies = data.supplies();
        if (completed) {
            requirePrerequisites(data, mission);
            points = FoundationAmounts.credit(points, mission.reward().points());
            supplies = FoundationAmounts.credit(supplies, mission.reward().supplies());
        }
        MissionState state = new MissionState(next, completed);
        boolean changed = data.saveMission(mission.id(), state, points, ProgressionRules.stageFor(points, thresholds), supplies);
        return new Update(mission, state, changed);
    }

    private static long observed(HumanWorldData data, Mission mission, long[] thresholds) {
        return switch (mission.objective()) {
            case SUPPLY_TOTAL -> data.supplies();
            case RESEARCH_COMPLETION -> data.isResearchComplete(mission.subject()) ? 1 : 0;
            case HUMAN_STAGE -> ProgressionRules.stageFor(data.points(), thresholds).level();
            case COMMAND_POST -> data.hasCommandPosts() ? 1 : 0;
            default -> 0; // Physical gameplay hooks belong to the sample/building/defence phases.
        };
    }

    private static void requirePrerequisites(HumanWorldData data, Mission mission) {
        for (String id : mission.prerequisites()) {
            if (data.mission(id).filter(MissionState::completed).isEmpty()) {
                throw new IllegalArgumentException("Requires completed mission: " + id);
            }
        }
    }

    public record Update(Mission mission, MissionState state, boolean changed) {
        public String message() {
            if (!changed) {
                return "Mission unchanged: " + mission.title() + (state.completed() ? " (already complete; no rewards applied)." : " (already active).");
            }
            if (state.completed()) {
                return "Mission complete: " + mission.title() + ". Reward: +" + mission.reward().points()
                        + " human points, +" + mission.reward().supplies() + " supplies.";
            }
            return "Mission progress: " + mission.title() + " " + state.progress() + "/" + mission.target();
        }
    }
}
