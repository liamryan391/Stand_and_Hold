package com.liamryan.standandhold.common.mission;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.util.ParasiteSampleHelper;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MissionManager {
    private MissionManager() {
    }

    public static List<Mission> getMissions() {
        String[] configuredEntries = StandAndHoldConfig.missions.missionEntries;
        if (configuredEntries == null || configuredEntries.length == 0) {
            return Collections.emptyList();
        }

        List<Mission> missions = new ArrayList<Mission>();
        Set<String> seenIds = new LinkedHashSet<String>();
        for (String configuredEntry : configuredEntries) {
            Mission mission = parseMission(configuredEntry);
            if (mission == null) {
                continue;
            }

            if (seenIds.add(mission.getId())) {
                missions.add(mission);
            } else if (StandAndHoldConfig.debugLogging) {
                StandAndHold.LOGGER.warn("Ignoring duplicate mission id '{}'.", mission.getId());
            }
        }
        return Collections.unmodifiableList(missions);
    }

    @Nullable
    public static Mission getMission(String id) {
        String normalizedId = Mission.normalizeId(id);
        if (normalizedId.isEmpty()) {
            return null;
        }

        for (Mission mission : getMissions()) {
            if (mission.getId().equals(normalizedId)) {
                return mission;
            }
        }
        return null;
    }

    public static List<String> getMissionIds() {
        List<String> ids = new ArrayList<String>();
        for (Mission mission : getMissions()) {
            ids.add(mission.getId());
        }
        return ids;
    }

    public static MissionStartResult startMission(World world, String id, @Nullable EntityPlayer player) {
        Mission mission = getMission(id);
        if (mission == null) {
            return MissionStartResult.unknown();
        }

        HumanWorldData data = HumanPointManager.getData(world);
        MissionProgress existingProgress = data.getMissionProgress(mission.getId());
        if (existingProgress != null) {
            return existingProgress.isCompleted() ? MissionStartResult.alreadyCompleted(mission) : MissionStartResult.alreadyActive(mission, existingProgress);
        }

        data.startMission(mission.getId(), world.getTotalWorldTime());
        refreshProgress(world, mission, player);
        StandAndHold.LOGGER.info("Mission started: {}.", mission.getId());
        return MissionStartResult.started(mission, data.getMissionProgress(mission.getId()));
    }

    public static MissionProgressResult addProgress(World world, String id, int amount) {
        Mission mission = getMission(id);
        if (mission == null) {
            return MissionProgressResult.unknown();
        }

        HumanWorldData data = HumanPointManager.getData(world);
        MissionProgress progress = data.getMissionProgress(mission.getId());
        if (progress == null) {
            return MissionProgressResult.notActive(mission);
        }

        if (progress.isCompleted()) {
            return MissionProgressResult.alreadyCompleted(mission, progress);
        }

        data.addMissionProgress(mission.getId(), amount);
        return MissionProgressResult.updated(mission, data.getMissionProgress(mission.getId()));
    }

    public static int recordObjectiveProgress(World world, MissionObjectiveType objectiveType, int amount) {
        if (world == null || world.isRemote || objectiveType == null || amount <= 0) {
            return 0;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        int updatedMissions = 0;
        for (Mission mission : getMissions()) {
            if (mission.getObjectiveType() != objectiveType && !isStructureObjectiveMatch(mission.getObjectiveType(), objectiveType)) {
                continue;
            }

            MissionProgress progress = data.getMissionProgress(mission.getId());
            if (progress == null || progress.isCompleted()) {
                continue;
            }

            int newProgress = Math.min(mission.getRequiredCount(), progress.getProgress() + amount);
            if (data.setMissionProgress(mission.getId(), newProgress)) {
                updatedMissions++;
            }
        }
        return updatedMissions;
    }

    public static int recordParasiteSampleRecovery(World world, EntityPlayer player, int recoveredSamples) {
        int updatedMissions = recordObjectiveProgress(world, MissionObjectiveType.RECOVER_PARASITE_SAMPLE, recoveredSamples);
        for (Mission mission : getMissions()) {
            if (mission.getObjectiveType() == MissionObjectiveType.RECOVER_PARASITE_SAMPLE) {
                MissionProgress progress = refreshProgress(world, mission, player);
                if (progress != null) {
                    updatedMissions++;
                }
            }
        }
        return updatedMissions;
    }

    public static int recordOutpostDefense(World world) {
        return recordObjectiveProgress(world, MissionObjectiveType.DEFEND_OUTPOST, 1);
    }

    public static int recordStructureDiscovery(World world, MissionObjectiveType structureType) {
        MissionObjectiveType safeStructureType = structureType == null ? MissionObjectiveType.DISCOVER_STRUCTURE : structureType;
        return recordObjectiveProgress(world, safeStructureType, 1);
    }

    public static MissionCompletionResult completeMission(World world, String id, @Nullable EntityPlayer player, boolean force) {
        Mission mission = getMission(id);
        if (mission == null) {
            return MissionCompletionResult.unknown();
        }

        HumanWorldData data = HumanPointManager.getData(world);
        MissionProgress progress = data.getMissionProgress(mission.getId());
        if (progress == null) {
            return MissionCompletionResult.notActive(mission);
        }

        if (progress.isCompleted()) {
            return MissionCompletionResult.alreadyCompleted(mission, progress);
        }

        refreshProgress(world, mission, player);
        progress = data.getMissionProgress(mission.getId());
        if (!force && progress.getProgress() < mission.getRequiredCount()) {
            return MissionCompletionResult.incomplete(mission, progress);
        }

        data.completeMission(mission.getId(), world.getTotalWorldTime());
        int totalPoints = mission.getPointReward() > 0 ? HumanPointManager.addPoints(world, mission.getPointReward(), "mission completion: " + mission.getId()) : data.getHumanPoints();
        int totalSupplies = mission.getSupplyReward() > 0 ? SupplyManager.addSupplies(world, mission.getSupplyReward(), "mission completion: " + mission.getId()) : data.getSupplyPoints();
        List<String> awardedResearch = awardResearchRewards(world, mission);
        StandAndHold.LOGGER.info("Mission completed: {}.", mission.getId());
        return MissionCompletionResult.completed(mission, data.getMissionProgress(mission.getId()), totalPoints, totalSupplies, awardedResearch);
    }

    public static MissionProgress refreshProgress(World world, Mission mission, @Nullable EntityPlayer player) {
        if (world == null || mission == null || world.isRemote) {
            return null;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        MissionProgress progress = data.getMissionProgress(mission.getId());
        if (progress == null || progress.isCompleted()) {
            return progress;
        }

        int trackedProgress = progress.getProgress();
        if (mission.getObjectiveType() == MissionObjectiveType.RECOVER_PARASITE_SAMPLE) {
            trackedProgress = Math.max(trackedProgress, Math.min(mission.getRequiredCount(), ParasiteSampleHelper.getAvailableParasiteSamples(player)));
        }
        data.setMissionProgress(mission.getId(), trackedProgress);
        return data.getMissionProgress(mission.getId());
    }

    private static boolean isStructureObjectiveMatch(MissionObjectiveType missionType, MissionObjectiveType eventType) {
        return missionType == MissionObjectiveType.DISCOVER_STRUCTURE
                && (eventType == MissionObjectiveType.DISCOVER_CHECKPOINT || eventType == MissionObjectiveType.DISCOVER_MAIN_BASE);
    }

    private static List<String> awardResearchRewards(World world, Mission mission) {
        if (!mission.hasResearchRewards()) {
            return Collections.emptyList();
        }

        List<String> awardedResearch = new ArrayList<String>();
        for (String researchId : mission.getResearchRewardIds()) {
            if (ResearchManager.completeResearchAsReward(world, researchId, "mission completion: " + mission.getId())) {
                awardedResearch.add(researchId);
            }
        }
        return Collections.unmodifiableList(awardedResearch);
    }

    @Nullable
    private static Mission parseMission(String configuredEntry) {
        if (configuredEntry == null || configuredEntry.trim().isEmpty()) {
            return null;
        }

        String[] parts = configuredEntry.split("\\|", -1);
        if (parts.length < 7) {
            StandAndHold.LOGGER.warn("Ignoring invalid mission entry '{}'. Expected at least 7 pipe-separated fields.", configuredEntry);
            return null;
        }

        try {
            return new Mission(
                    parts[0],
                    parts[1],
                    parts[2],
                    MissionObjectiveType.fromConfigValue(parts[3]),
                    parseNonNegativeInt(parts[4], 1),
                    parseNonNegativeInt(parts[5], 0),
                    parseNonNegativeInt(parts[6], 0),
                    parseResearchRewardIds(parts.length >= 8 ? parts[7] : "")
            );
        } catch (IllegalArgumentException exception) {
            StandAndHold.LOGGER.warn("Ignoring invalid mission entry '{}': {}", configuredEntry, exception.getMessage());
            return null;
        }
    }

    private static int parseNonNegativeInt(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Math.max(0, Integer.parseInt(rawValue.trim()));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static List<String> parseResearchRewardIds(String rawResearchRewardIds) {
        if (rawResearchRewardIds == null || rawResearchRewardIds.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> researchIds = new ArrayList<String>();
        String[] rawIds = rawResearchRewardIds.split(",");
        for (String rawId : rawIds) {
            String normalizedId = ResearchEntry.normalizeId(rawId);
            if (!normalizedId.isEmpty()) {
                researchIds.add(normalizedId);
            }
        }
        return researchIds;
    }

    public enum MissionStartStatus {
        STARTED,
        ALREADY_ACTIVE,
        ALREADY_COMPLETED,
        UNKNOWN_MISSION
    }

    public enum MissionProgressStatus {
        UPDATED,
        NOT_ACTIVE,
        ALREADY_COMPLETED,
        UNKNOWN_MISSION
    }

    public enum MissionCompletionStatus {
        COMPLETED,
        NOT_ACTIVE,
        INCOMPLETE,
        ALREADY_COMPLETED,
        UNKNOWN_MISSION
    }

    public static final class MissionStartResult {
        private final MissionStartStatus status;
        private final Mission mission;
        private final MissionProgress progress;

        private MissionStartResult(MissionStartStatus status, Mission mission, MissionProgress progress) {
            this.status = status;
            this.mission = mission;
            this.progress = progress;
        }

        private static MissionStartResult started(Mission mission, MissionProgress progress) {
            return new MissionStartResult(MissionStartStatus.STARTED, mission, progress);
        }

        private static MissionStartResult alreadyActive(Mission mission, MissionProgress progress) {
            return new MissionStartResult(MissionStartStatus.ALREADY_ACTIVE, mission, progress);
        }

        private static MissionStartResult alreadyCompleted(Mission mission) {
            return new MissionStartResult(MissionStartStatus.ALREADY_COMPLETED, mission, null);
        }

        private static MissionStartResult unknown() {
            return new MissionStartResult(MissionStartStatus.UNKNOWN_MISSION, null, null);
        }

        public MissionStartStatus getStatus() {
            return status;
        }

        public Mission getMission() {
            return mission;
        }

        public MissionProgress getProgress() {
            return progress;
        }
    }

    public static final class MissionProgressResult {
        private final MissionProgressStatus status;
        private final Mission mission;
        private final MissionProgress progress;

        private MissionProgressResult(MissionProgressStatus status, Mission mission, MissionProgress progress) {
            this.status = status;
            this.mission = mission;
            this.progress = progress;
        }

        private static MissionProgressResult updated(Mission mission, MissionProgress progress) {
            return new MissionProgressResult(MissionProgressStatus.UPDATED, mission, progress);
        }

        private static MissionProgressResult notActive(Mission mission) {
            return new MissionProgressResult(MissionProgressStatus.NOT_ACTIVE, mission, null);
        }

        private static MissionProgressResult alreadyCompleted(Mission mission, MissionProgress progress) {
            return new MissionProgressResult(MissionProgressStatus.ALREADY_COMPLETED, mission, progress);
        }

        private static MissionProgressResult unknown() {
            return new MissionProgressResult(MissionProgressStatus.UNKNOWN_MISSION, null, null);
        }

        public MissionProgressStatus getStatus() {
            return status;
        }

        public Mission getMission() {
            return mission;
        }

        public MissionProgress getProgress() {
            return progress;
        }
    }

    public static final class MissionCompletionResult {
        private final MissionCompletionStatus status;
        private final Mission mission;
        private final MissionProgress progress;
        private final int totalHumanPoints;
        private final int totalSupplies;
        private final List<String> awardedResearchIds;

        private MissionCompletionResult(MissionCompletionStatus status, Mission mission, MissionProgress progress, int totalHumanPoints, int totalSupplies, List<String> awardedResearchIds) {
            this.status = status;
            this.mission = mission;
            this.progress = progress;
            this.totalHumanPoints = totalHumanPoints;
            this.totalSupplies = totalSupplies;
            this.awardedResearchIds = awardedResearchIds == null ? Collections.<String>emptyList() : Collections.unmodifiableList(awardedResearchIds);
        }

        private static MissionCompletionResult completed(Mission mission, MissionProgress progress, int totalHumanPoints, int totalSupplies, List<String> awardedResearchIds) {
            return new MissionCompletionResult(MissionCompletionStatus.COMPLETED, mission, progress, totalHumanPoints, totalSupplies, awardedResearchIds);
        }

        private static MissionCompletionResult notActive(Mission mission) {
            return new MissionCompletionResult(MissionCompletionStatus.NOT_ACTIVE, mission, null, 0, 0, Collections.<String>emptyList());
        }

        private static MissionCompletionResult incomplete(Mission mission, MissionProgress progress) {
            return new MissionCompletionResult(MissionCompletionStatus.INCOMPLETE, mission, progress, 0, 0, Collections.<String>emptyList());
        }

        private static MissionCompletionResult alreadyCompleted(Mission mission, MissionProgress progress) {
            return new MissionCompletionResult(MissionCompletionStatus.ALREADY_COMPLETED, mission, progress, 0, 0, Collections.<String>emptyList());
        }

        private static MissionCompletionResult unknown() {
            return new MissionCompletionResult(MissionCompletionStatus.UNKNOWN_MISSION, null, null, 0, 0, Collections.<String>emptyList());
        }

        public MissionCompletionStatus getStatus() {
            return status;
        }

        public Mission getMission() {
            return mission;
        }

        public MissionProgress getProgress() {
            return progress;
        }

        public int getTotalHumanPoints() {
            return totalHumanPoints;
        }

        public int getTotalSupplies() {
            return totalSupplies;
        }

        public List<String> getAwardedResearchIds() {
            return awardedResearchIds;
        }
    }
}
