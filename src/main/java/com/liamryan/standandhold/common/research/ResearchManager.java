package com.liamryan.standandhold.common.research;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.progression.HumanPointManager;
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

public final class ResearchManager {
    private ResearchManager() {
    }

    public static List<ResearchEntry> getResearchEntries() {
        String[] configuredEntries = StandAndHoldConfig.research.researchEntries;
        if (configuredEntries == null || configuredEntries.length == 0) {
            return Collections.emptyList();
        }

        List<ResearchEntry> entries = new ArrayList<ResearchEntry>();
        Set<String> seenIds = new LinkedHashSet<String>();
        for (String configuredEntry : configuredEntries) {
            ResearchEntry entry = parseEntry(configuredEntry);
            if (entry == null) {
                continue;
            }

            if (seenIds.add(entry.getId())) {
                entries.add(entry);
            } else if (StandAndHoldConfig.debugLogging) {
                StandAndHold.LOGGER.warn("Ignoring duplicate research entry id '{}'.", entry.getId());
            }
        }

        return Collections.unmodifiableList(entries);
    }

    public static ResearchEntry getResearchEntry(String id) {
        String normalizedId = ResearchEntry.normalizeId(id);
        if (normalizedId.isEmpty()) {
            return null;
        }

        for (ResearchEntry entry : getResearchEntries()) {
            if (entry.getId().equals(normalizedId)) {
                return entry;
            }
        }

        return null;
    }

    public static List<String> getResearchIds() {
        List<String> ids = new ArrayList<String>();
        for (ResearchEntry entry : getResearchEntries()) {
            ids.add(entry.getId());
        }
        return ids;
    }

    public static boolean isResearchComplete(World world, String id) {
        ResearchEntry entry = getResearchEntry(id);
        return entry != null && HumanPointManager.getData(world).isResearchCompleted(entry.getId());
    }

    public static CompletionResult completeResearch(World world, String id) {
        return completeResearch(world, id, null, false);
    }

    public static CompletionResult completeResearch(World world, String id, @Nullable EntityPlayer player) {
        return completeResearch(world, id, player, player != null);
    }

    private static CompletionResult completeResearch(World world, String id, @Nullable EntityPlayer player, boolean enforceSampleCost) {
        ResearchEntry entry = getResearchEntry(id);
        if (entry == null) {
            return CompletionResult.unknown();
        }

        HumanWorldData data = HumanPointManager.getData(world);
        if (data.isResearchCompleted(entry.getId())) {
            return CompletionResult.alreadyComplete(entry);
        }

        List<String> missingRequirements = getMissingRequirements(data, entry);
        if (!missingRequirements.isEmpty()) {
            return CompletionResult.missingRequirements(entry, missingRequirements);
        }

        int availableSamples = ParasiteSampleHelper.getAvailableParasiteSamples(player);
        if (enforceSampleCost && entry.getParasiteSampleCost() > 0 && availableSamples < entry.getParasiteSampleCost()) {
            return CompletionResult.missingSamples(entry, entry.getParasiteSampleCost(), availableSamples);
        }

        ParasiteSampleHelper.consumeParasiteSamples(player, entry.getParasiteSampleCost());
        data.completeResearch(entry.getId());
        if (entry.getCompletionPointReward() > 0) {
            HumanPointManager.addPoints(world, entry.getCompletionPointReward(), "research completion: " + entry.getId());
        }

        StandAndHold.LOGGER.info("Research completed: {}.", entry.getId());
        return CompletionResult.completed(entry);
    }

    public static int getCompletedResearchCount(World world) {
        return getCompletedResearchEntries(world).size();
    }

    public static List<ResearchEntry> getCompletedResearchEntries(World world) {
        HumanWorldData data = HumanPointManager.getData(world);
        List<ResearchEntry> completedEntries = new ArrayList<ResearchEntry>();
        for (ResearchEntry entry : getResearchEntries()) {
            if (data.isResearchCompleted(entry.getId())) {
                completedEntries.add(entry);
            }
        }
        return Collections.unmodifiableList(completedEntries);
    }

    public static List<String> getMissingRequirements(HumanWorldData data, ResearchEntry entry) {
        List<String> missingRequirements = new ArrayList<String>();
        for (String requiredResearchId : entry.getRequiredResearchIds()) {
            if (!data.isResearchCompleted(requiredResearchId)) {
                missingRequirements.add(requiredResearchId);
            }
        }
        return missingRequirements;
    }

    private static ResearchEntry parseEntry(String configuredEntry) {
        if (configuredEntry == null || configuredEntry.trim().isEmpty()) {
            return null;
        }

        String[] parts = configuredEntry.split("\\|", -1);
        if (parts.length < 5) {
            StandAndHold.LOGGER.warn("Ignoring invalid research entry '{}'. Expected at least 5 pipe-separated fields.", configuredEntry);
            return null;
        }

        try {
            return new ResearchEntry(
                    parts[0],
                    ResearchCategory.fromConfigValue(parts[1]),
                    parts[2],
                    parts[3],
                    parsePointReward(parts[4]),
                    parseRequirements(parts.length >= 6 ? parts[5] : ""),
                    parseSampleCost(parts.length >= 7 ? parts[6] : "")
            );
        } catch (IllegalArgumentException exception) {
            StandAndHold.LOGGER.warn("Ignoring invalid research entry '{}': {}", configuredEntry, exception.getMessage());
            return null;
        }
    }

    private static int parseSampleCost(String rawSampleCost) {
        if (rawSampleCost == null || rawSampleCost.trim().isEmpty()) {
            return 0;
        }

        try {
            return Math.max(0, Integer.parseInt(rawSampleCost.trim()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static int parsePointReward(String rawPointReward) {
        if (rawPointReward == null || rawPointReward.trim().isEmpty()) {
            return 0;
        }

        try {
            return Math.max(0, Integer.parseInt(rawPointReward.trim()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static List<String> parseRequirements(String rawRequirements) {
        if (rawRequirements == null || rawRequirements.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> requirements = new ArrayList<String>();
        String[] rawIds = rawRequirements.split(",");
        for (String rawId : rawIds) {
            String normalizedId = ResearchEntry.normalizeId(rawId);
            if (!normalizedId.isEmpty()) {
                requirements.add(normalizedId);
            }
        }
        return requirements;
    }

    public static final class CompletionResult {
        private final CompletionStatus status;
        private final ResearchEntry entry;
        private final List<String> missingRequirements;
        private final int requiredSamples;
        private final int availableSamples;

        private CompletionResult(CompletionStatus status, ResearchEntry entry, List<String> missingRequirements, int requiredSamples, int availableSamples) {
            this.status = status;
            this.entry = entry;
            this.missingRequirements = missingRequirements == null ? Collections.<String>emptyList() : Collections.unmodifiableList(missingRequirements);
            this.requiredSamples = requiredSamples;
            this.availableSamples = availableSamples;
        }

        public static CompletionResult unknown() {
            return new CompletionResult(CompletionStatus.UNKNOWN_RESEARCH, null, Collections.<String>emptyList(), 0, 0);
        }

        public static CompletionResult alreadyComplete(ResearchEntry entry) {
            return new CompletionResult(CompletionStatus.ALREADY_COMPLETE, entry, Collections.<String>emptyList(), 0, 0);
        }

        public static CompletionResult missingRequirements(ResearchEntry entry, List<String> missingRequirements) {
            return new CompletionResult(CompletionStatus.MISSING_REQUIREMENTS, entry, missingRequirements, 0, 0);
        }

        public static CompletionResult missingSamples(ResearchEntry entry, int requiredSamples, int availableSamples) {
            return new CompletionResult(CompletionStatus.MISSING_SAMPLES, entry, Collections.<String>emptyList(), requiredSamples, availableSamples);
        }

        public static CompletionResult completed(ResearchEntry entry) {
            return new CompletionResult(CompletionStatus.COMPLETED, entry, Collections.<String>emptyList(), 0, 0);
        }

        public CompletionStatus getStatus() {
            return status;
        }

        public ResearchEntry getEntry() {
            return entry;
        }

        public List<String> getMissingRequirements() {
            return missingRequirements;
        }

        public int getRequiredSamples() {
            return requiredSamples;
        }

        public int getAvailableSamples() {
            return availableSamples;
        }
    }

    public enum CompletionStatus {
        COMPLETED,
        ALREADY_COMPLETE,
        MISSING_REQUIREMENTS,
        MISSING_SAMPLES,
        UNKNOWN_RESEARCH
    }
}
