package com.liamryan.standandhold.common.mission;

import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public enum Change { STARTED, PROGRESS, COMPLETED, BLOCKED }

    /** One observable result of {@link #sync}. Completion is reported once because the saved receipt changes once. */
    public record Notice(Change change, Mission mission, MissionState state, String detail) {
        public String text() {
            return switch (change) {
                case STARTED -> "New order: " + mission.title() + " " + state.progress() + "/" + mission.target();
                case PROGRESS -> "Order progress: " + mission.title() + " " + state.progress() + "/" + mission.target();
                case COMPLETED -> "Order complete: " + mission.title() + ". Reward: +" + mission.reward().points()
                        + " human points, +" + mission.reward().supplies() + " supplies.";
                case BLOCKED -> "Order update blocked: " + mission.id() + " - " + detail;
            };
        }
    }

    /**
     * Gameplay entry point: catalog-flagged onboarding orders start themselves once their prerequisites are complete,
     * then every active order is re-evaluated. {@code carried} is the actor's current Tissue Sample count, or -1 when
     * no inventory was observed. Bounded to one pass per catalog entry; never ticks, scans the world or loads chunks.
     */
    public static List<Notice> sync(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, int carried) {
        if (carried < -1) { throw new IllegalArgumentException("Negative carried sample count."); }
        data.requireSupportedVersion();
        List<Notice> notices = new ArrayList<>();
        Set<String> blocked = new HashSet<>();
        boolean changed = true;
        for (int pass = 0; changed && pass <= catalog.missions().size(); pass++) {
            changed = false;
            for (Mission mission : catalog.missions().values()) {
                if (blocked.contains(mission.id())) { continue; }
                try {
                    var state = data.mission(mission.id());
                    if (state.isPresent() && state.get().completed()) { continue; }
                    boolean starting = state.isEmpty();
                    if (starting && (!mission.autoStart() || !prerequisitesComplete(data, mission))) { continue; }
                    long observed = mission.objective() == Mission.Objective.SAMPLE_RECOVERY ? Math.max(0, carried) : observed(data, mission, thresholds);
                    Update update = update(data, mission, thresholds, observed);
                    if (!update.changed()) { continue; }
                    changed = true;
                    notices.add(new Notice(update.state().completed() ? Change.COMPLETED : starting ? Change.STARTED : Change.PROGRESS,
                            mission, update.state(), ""));
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    blocked.add(mission.id());
                    notices.add(new Notice(Change.BLOCKED, mission, null, String.valueOf(exception.getMessage())));
                }
            }
        }
        return List.copyOf(notices);
    }

    /** The order players should work on: first catalog entry not completed whose prerequisites are complete. Read-only. */
    public static Optional<Mission> current(HumanWorldData data, FoundationCatalog catalog) {
        return catalog.missions().values().stream()
                .filter(mission -> data.mission(mission.id()).filter(MissionState::completed).isEmpty())
                .filter(mission -> prerequisitesComplete(data, mission)).findFirst();
    }

    public static boolean prerequisitesComplete(HumanWorldData data, Mission mission) {
        for (String id : mission.prerequisites()) {
            if (data.mission(id).filter(MissionState::completed).isEmpty()) { return false; }
        }
        return true;
    }

    /** Highest actual carried count, never cumulative pickup events: dropping/re-picking cannot farm progress. */
    public static List<String> observeSamples(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, int carried) {
        if (carried < 0) { throw new IllegalArgumentException("Negative carried sample count."); }
        List<String> messages = new ArrayList<>();
        for (Mission mission : catalog.missions().values()) {
            if (mission.objective() != Mission.Objective.SAMPLE_RECOVERY) { continue; }
            try {
                var state = data.mission(mission.id());
                if (state.isEmpty() || state.get().completed()) { continue; }
                Update update = update(data, mission, thresholds, carried);
                if (update.changed()) { messages.add(update.message()); }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                messages.add("Mission update blocked: " + mission.id() + " - " + exception.getMessage());
            }
        }
        return List.copyOf(messages);
    }

    /** Called once for a reserved raider killed by a player/human, never for /kill or unload. */
    public static List<String> observeDefence(HumanWorldData data, FoundationCatalog catalog, long[] thresholds) {
        List<String> messages = new ArrayList<>();
        for (Mission mission : catalog.missions().values()) {
            if (mission.objective() != Mission.Objective.OUTPOST_DEFENCE) { continue; }
            var state = data.mission(mission.id());
            if (state.isEmpty() || state.get().completed()) { continue; }
            try {
                var result = advance(data, catalog, thresholds, mission.id(), 1);
                if (result.changed()) { messages.add(result.message()); }
            } catch (IllegalArgumentException | IllegalStateException exception) { messages.add("Defence mission blocked: " + exception.getMessage()); }
        }
        return List.copyOf(messages);
    }

    private static long observed(HumanWorldData data, Mission mission, long[] thresholds) {
        return switch (mission.objective()) {
            case SUPPLY_TOTAL -> data.supplies();
            case RESEARCH_COMPLETION -> data.isResearchComplete(mission.subject()) ? 1 : 0;
            case HUMAN_STAGE -> ProgressionRules.stageFor(data.points(), thresholds).level();
            case COMMAND_POST -> data.hasCommandPosts() ? 1 : 0;
            case MAIN_BASE_DISCOVERY -> data.hasDiscoveredMainBase() ? 1 : 0;
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
