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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MissionManager {
    private static final String[] CORE_MISSION_ENTRIES = new String[] {
            "recover_parasite_sample|Recover Parasite Samples|Recover and catalog the first parasite tissue sample for the human resistance.|RECOVER_PARASITE_SAMPLE|1|20|8|",
            "establish_field_command|Establish Field Command|Place, discover, or open a Field Command Post so survivors have a command anchor.|ESTABLISH_FIELD_COMMAND|1|25|12|",
            "stockpile_supplies|Stockpile Supplies|Build a small supply reserve through Supply Crates or building transfers.|STOCKPILE_SUPPLIES|16|20|0|",
            "complete_first_research|Complete First Research|Complete one research entry through a Research Lab or admin research command.|COMPLETE_RESEARCH|1|40|10|",
            "reach_local_response|Reach Local Army Response|Reach Human Stage 1 to begin the local army response.|REACH_HUMAN_STAGE|1|0|25|",
            "defend_outpost|Defend the Outpost|Trigger or survive one outpost attack event near a command post.|DEFEND_OUTPOST|1|40|20|",
            "establish_main_base|Establish Main Base|Generate, discover, or register a Main Base foundation for later escalation.|DISCOVER_MAIN_BASE|1|100|80|"
    };

    private static int cachedMissionEntriesHash = Integer.MIN_VALUE;
    private static List<Mission> cachedMissions = Collections.emptyList();

    private MissionManager() {
    }

    public static List<Mission> getMissions() {
        String[] configuredEntries = StandAndHoldConfig.missions.missionEntries;
        int entriesHash = 31 * Arrays.hashCode(configuredEntries) + Arrays.hashCode(CORE_MISSION_ENTRIES);
        if (entriesHash == cachedMissionEntriesHash) {
            return cachedMissions;
        }

        List<Mission> missions = new ArrayList<Mission>();
        Set<String> seenIds = new LinkedHashSet<String>();
        addMissionEntries(configuredEntries, missions, seenIds, true);
        addMissionEntries(CORE_MISSION_ENTRIES, missions, seenIds, false);

        cachedMissionEntriesHash = entriesHash;
        cachedMissions = Collections.unmodifiableList(missions);
        return cachedMissions;
    }

    private static void addMissionEntries(String[] configuredEntries, List<Mission> missions, Set<String> seenIds, boolean logDuplicates) {
        if (configuredEntries == null) {
            return;
        }

        for (String configuredEntry : configuredEntries) {
            Mission mission = parseMission(configuredEntry);
            if (mission == null) {
                continue;
            }

            if (seenIds.add(mission.getId())) {
                missions.add(mission);
            } else if (logDuplicates && StandAndHoldConfig.debugLogging) {
                StandAndHold.LOGGER.warn("Ignoring duplicate mission id '{}'.", mission.getId());
            }
        }
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
        MissionProgress progress = getOrStartMissionProgress(data, mission, world.getTotalWorldTime());
        if (progress == null) {
            return MissionProgressResult.notActive(mission);
        }
        if (progress.isCompleted()) {
            return MissionProgressResult.alreadyCompleted(mission, progress);
        }

        data.addMissionProgress(mission.getId(), amount);
        progress = data.getMissionProgress(mission.getId());
        if (progress != null && progress.getProgress() >= mission.getRequiredCount()) {
            completeMissionFromProgress(world, mission, null);
            progress = data.getMissionProgress(mission.getId());
        }
        return MissionProgressResult.updated(mission, progress);
    }

    public static int recordObjectiveProgress(World world, MissionObjectiveType objectiveType, int amount) {
        return recordObjectiveProgress(world, objectiveType, amount, null);
    }

    public static int recordObjectiveProgress(World world, MissionObjectiveType objectiveType, int amount, @Nullable EntityPlayer player) {
        if (world == null || world.isRemote || objectiveType == null || amount <= 0) {
            return 0;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        int updatedMissions = 0;
        for (Mission mission : getMissions()) {
            if (mission.getObjectiveType() != objectiveType && !isStructureObjectiveMatch(mission.getObjectiveType(), objectiveType)) {
                continue;
            }

            MissionProgress progress = getOrStartMissionProgress(data, mission, world.getTotalWorldTime());
            if (progress == null || progress.isCompleted()) {
                continue;
            }

            int newProgress = Math.min(mission.getRequiredCount(), progress.getProgress() + amount);
            if (data.setMissionProgress(mission.getId(), newProgress)) {
                updatedMissions++;
                progress = data.getMissionProgress(mission.getId());
                if (progress != null && progress.getProgress() >= mission.getRequiredCount()) {
                    completeMissionFromProgress(world, mission, player);
                } else {
                    sendProgressMessage(player, mission, progress);
                }
            }
        }
        return updatedMissions;
    }

    public static int recordParasiteSampleRecovery(World world, EntityPlayer player, int recoveredSamples) {
        int updatedMissions = recordObjectiveProgress(world, MissionObjectiveType.RECOVER_PARASITE_SAMPLE, recoveredSamples, player);
        for (Mission mission : getMissions()) {
            if (mission.getObjectiveType() == MissionObjectiveType.RECOVER_PARASITE_SAMPLE) {
                MissionProgress progress = refreshProgress(world, mission, player);
                if (progress != null) {
                    updatedMissions++;
                    if (!progress.isCompleted() && progress.getProgress() >= mission.getRequiredCount()) {
                        completeMissionFromProgress(world, mission, player);
                    }
                }
            }
        }
        return updatedMissions;
    }

    public static int recordOutpostDefense(World world) {
        return recordObjectiveProgress(world, MissionObjectiveType.DEFEND_OUTPOST, 1);
    }

    public static int recordFieldCommandPostEstablished(World world, @Nullable EntityPlayer player) {
        return recordObjectiveProgressToAtLeast(world, MissionObjectiveType.ESTABLISH_FIELD_COMMAND, 1, player);
    }

    public static int recordSupplyStockpile(World world, @Nullable EntityPlayer player, int observedSupplies) {
        int globalSupplies = SupplyManager.getSupplyPoints(world);
        return recordObjectiveProgressToAtLeast(world, MissionObjectiveType.STOCKPILE_SUPPLIES, Math.max(globalSupplies, observedSupplies), player);
    }

    public static int recordResearchCompleted(World world, @Nullable EntityPlayer player) {
        return recordObjectiveProgress(world, MissionObjectiveType.COMPLETE_RESEARCH, 1, player);
    }

    public static int recordHumanStageReached(World world, @Nullable EntityPlayer player) {
        if (world == null || world.isRemote) {
            return 0;
        }

        return recordObjectiveProgressToAtLeast(world, MissionObjectiveType.REACH_HUMAN_STAGE, HumanPointManager.getData(world).getStage().getId(), player);
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
        if (!force && progress != null && progress.getProgress() < mission.getRequiredCount()) {
            return MissionCompletionResult.incomplete(mission, progress);
        }

        data.completeMission(mission.getId(), world.getTotalWorldTime());
        int totalPoints = mission.getPointReward() > 0 ? HumanPointManager.addPoints(world, mission.getPointReward(), "mission completion: " + mission.getId()) : data.getHumanPoints();
        int totalSupplies = mission.getSupplyReward() > 0 ? SupplyManager.addSupplies(world, mission.getSupplyReward(), "mission completion: " + mission.getId()) : data.getSupplyPoints();
        List<String> awardedResearch = awardResearchRewards(world, mission);
        recordSupplyStockpile(world, player, totalSupplies);
        recordHumanStageReached(world, player);
        StandAndHold.LOGGER.info("Mission completed: {}.", mission.getId());
        return MissionCompletionResult.completed(mission, data.getMissionProgress(mission.getId()), totalPoints, totalSupplies, awardedResearch);
    }

    public static MissionProgress refreshProgress(World world, Mission mission, @Nullable EntityPlayer player) {
        if (world == null || mission == null || world.isRemote) {
            return null;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        MissionProgress progress = getOrStartMissionProgress(data, mission, world.getTotalWorldTime());
        if (progress == null || progress.isCompleted()) {
            return progress;
        }

        int trackedProgress = progress.getProgress();
        if (mission.getObjectiveType() == MissionObjectiveType.RECOVER_PARASITE_SAMPLE) {
            trackedProgress = Math.max(trackedProgress, Math.min(mission.getRequiredCount(), ParasiteSampleHelper.getAvailableParasiteSamples(player)));
        } else if (mission.getObjectiveType() == MissionObjectiveType.ESTABLISH_FIELD_COMMAND) {
            trackedProgress = Math.max(trackedProgress, getFieldCommandProgress(data, mission));
        } else if (mission.getObjectiveType() == MissionObjectiveType.STOCKPILE_SUPPLIES) {
            trackedProgress = Math.max(trackedProgress, Math.min(mission.getRequiredCount(), data.getSupplyPoints()));
        } else if (mission.getObjectiveType() == MissionObjectiveType.COMPLETE_RESEARCH) {
            trackedProgress = Math.max(trackedProgress, Math.min(mission.getRequiredCount(), ResearchManager.getCompletedResearchCount(world)));
        } else if (mission.getObjectiveType() == MissionObjectiveType.REACH_HUMAN_STAGE) {
            trackedProgress = Math.max(trackedProgress, Math.min(mission.getRequiredCount(), data.getStage().getId()));
        } else if (mission.getObjectiveType() == MissionObjectiveType.DISCOVER_MAIN_BASE) {
            trackedProgress = Math.max(trackedProgress, data.getMainBasePositions().isEmpty() ? 0 : Math.min(mission.getRequiredCount(), 1));
        }

        data.setMissionProgress(mission.getId(), trackedProgress);
        return data.getMissionProgress(mission.getId());
    }

    private static int recordObjectiveProgressToAtLeast(World world, MissionObjectiveType objectiveType, int observedProgress, @Nullable EntityPlayer player) {
        if (world == null || world.isRemote || objectiveType == null || observedProgress <= 0) {
            return 0;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        int updatedMissions = 0;
        for (Mission mission : getMissions()) {
            if (mission.getObjectiveType() != objectiveType && !isStructureObjectiveMatch(mission.getObjectiveType(), objectiveType)) {
                continue;
            }

            MissionProgress progress = getOrStartMissionProgress(data, mission, world.getTotalWorldTime());
            if (progress == null || progress.isCompleted()) {
                continue;
            }

            int newProgress = Math.max(progress.getProgress(), Math.min(mission.getRequiredCount(), observedProgress));
            if (data.setMissionProgress(mission.getId(), newProgress)) {
                updatedMissions++;
                progress = data.getMissionProgress(mission.getId());
                if (progress != null && progress.getProgress() >= mission.getRequiredCount()) {
                    completeMissionFromProgress(world, mission, player);
                } else {
                    sendProgressMessage(player, mission, progress);
                }
            }
        }
        return updatedMissions;
    }

    @Nullable
    private static MissionProgress getOrStartMissionProgress(HumanWorldData data, Mission mission, long worldTime) {
        MissionProgress progress = data.getMissionProgress(mission.getId());
        if (progress == null) {
            data.startMission(mission.getId(), worldTime);
            progress = data.getMissionProgress(mission.getId());
        }
        return progress;
    }

    private static int getFieldCommandProgress(HumanWorldData data, Mission mission) {
        return data.getFieldCommandPostPositions().isEmpty() ? 0 : Math.min(mission.getRequiredCount(), 1);
    }

    private static void completeMissionFromProgress(World world, Mission mission, @Nullable EntityPlayer player) {
        MissionCompletionResult result = completeMission(world, mission.getId(), player, false);
        if (result.getStatus() == MissionCompletionStatus.COMPLETED) {
            sendCompletionMessage(player, result);
        }
    }

    private static void sendProgressMessage(@Nullable EntityPlayer player, Mission mission, @Nullable MissionProgress progress) {
        if (player == null || progress == null) {
            return;
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.mission.progress",
                mission.getDisplayName(),
                progress.getProgress(),
                mission.getRequiredCount()
        );
        message.getStyle().setColor(TextFormatting.AQUA);
        player.sendMessage(message);
    }

    private static void sendCompletionMessage(@Nullable EntityPlayer player, MissionCompletionResult result) {
        if (player == null || result == null || result.getMission() == null) {
            return;
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.mission.complete",
                result.getMission().getDisplayName(),
                result.getMission().getPointReward(),
                result.getMission().getSupplyReward(),
                result.getAwardedResearchIds().isEmpty() ? "none" : joinStrings(result.getAwardedResearchIds())
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        player.sendMessage(message);
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

    private static String joinStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
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
