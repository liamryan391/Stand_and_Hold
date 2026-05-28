package com.liamryan.standandhold.common.research;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.util.ParasiteSampleHelper;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ResearchManager {
    private static final String[] CORE_RESEARCH_ENTRIES = new String[] {
            "parasite_samples|PARASITE_BIOLOGY|Parasite Samples|Catalog recovered parasite tissue and establish basic containment procedures.|15||1|0",
            "field_communications|MILITARY_LOGISTICS|Field Communications|Coordinate survivor cells and local army response teams across infected territory.|20||0|20",
            "outpost_doctrine|BASE_INFRASTRUCTURE|Outpost Doctrine|Draft the first defensible outpost standards for later military construction.|35|field_communications|0|50",
            "special_division_training|SPECIAL_PROJECTS|Special Division Training|Train select operatives for anti-parasite rapid deployment and containment work.|100|parasite_samples,outpost_doctrine|4|120"
    };

    private static int cachedResearchEntriesHash = Integer.MIN_VALUE;
    private static List<ResearchEntry> cachedResearchEntries = Collections.emptyList();

    private ResearchManager() {
    }

    public static List<ResearchEntry> getResearchEntries() {
        String[] configuredEntries = StandAndHoldConfig.research.researchEntries;
        int entriesHash = 31 * Arrays.hashCode(configuredEntries) + Arrays.hashCode(CORE_RESEARCH_ENTRIES);
        if (entriesHash == cachedResearchEntriesHash) {
            return cachedResearchEntries;
        }

        List<ResearchEntry> entries = new ArrayList<ResearchEntry>();
        Set<String> seenIds = new LinkedHashSet<String>();
        addResearchEntries(configuredEntries, entries, seenIds, true);
        addResearchEntries(CORE_RESEARCH_ENTRIES, entries, seenIds, false);

        cachedResearchEntriesHash = entriesHash;
        cachedResearchEntries = Collections.unmodifiableList(entries);
        return cachedResearchEntries;
    }

    private static void addResearchEntries(String[] configuredEntries, List<ResearchEntry> entries, Set<String> seenIds, boolean logDuplicates) {
        if (configuredEntries == null) {
            return;
        }

        for (String configuredEntry : configuredEntries) {
            ResearchEntry entry = parseEntry(configuredEntry);
            if (entry == null) {
                continue;
            }

            if (seenIds.add(entry.getId())) {
                entries.add(entry);
            } else if (logDuplicates && StandAndHoldConfig.debugLogging) {
                StandAndHold.LOGGER.warn("Ignoring duplicate research entry id '{}'.", entry.getId());
            }
        }
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

    public static boolean completeResearchAsReward(World world, String id, String source) {
        ResearchEntry entry = getResearchEntry(id);
        if (entry == null) {
            return false;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        if (!data.completeResearch(entry.getId())) {
            return false;
        }

        if (entry.getCompletionPointReward() > 0) {
            HumanPointManager.addPoints(world, entry.getCompletionPointReward(), source + " research reward: " + entry.getId());
        }
        MissionManager.recordResearchCompleted(world, null);
        StandAndHold.LOGGER.info("Research completed as reward from {}: {}.", source, entry.getId());
        return true;
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

        int availableSupplies = SupplyManager.getSupplyPoints(world);
        if (entry.getSupplyCost() > 0 && availableSupplies < entry.getSupplyCost()) {
            return CompletionResult.missingSupplies(entry, entry.getSupplyCost(), availableSupplies);
        }

        ParasiteSampleHelper.consumeParasiteSamples(player, entry.getParasiteSampleCost());
        SupplyManager.spendSupplies(world, entry.getSupplyCost(), "research completion: " + entry.getId());
        data.completeResearch(entry.getId());
        if (entry.getCompletionPointReward() > 0) {
            HumanPointManager.addPoints(world, entry.getCompletionPointReward(), "research completion: " + entry.getId());
        }
        MissionManager.recordResearchCompleted(world, player);

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
                    parseNonNegativeInt(parts.length >= 7 ? parts[6] : ""),
                    parseNonNegativeInt(parts.length >= 8 ? parts[7] : "")
            );
        } catch (IllegalArgumentException exception) {
            StandAndHold.LOGGER.warn("Ignoring invalid research entry '{}': {}", configuredEntry, exception.getMessage());
            return null;
        }
    }

    private static int parseNonNegativeInt(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0;
        }

        try {
            return Math.max(0, Integer.parseInt(rawValue.trim()));
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
        private final int requiredSupplies;
        private final int availableSupplies;

        private CompletionResult(CompletionStatus status, ResearchEntry entry, List<String> missingRequirements, int requiredSamples, int availableSamples, int requiredSupplies, int availableSupplies) {
            this.status = status;
            this.entry = entry;
            this.missingRequirements = missingRequirements == null ? Collections.<String>emptyList() : Collections.unmodifiableList(missingRequirements);
            this.requiredSamples = requiredSamples;
            this.availableSamples = availableSamples;
            this.requiredSupplies = requiredSupplies;
            this.availableSupplies = availableSupplies;
        }

        public static CompletionResult unknown() {
            return new CompletionResult(CompletionStatus.UNKNOWN_RESEARCH, null, Collections.<String>emptyList(), 0, 0, 0, 0);
        }

        public static CompletionResult alreadyComplete(ResearchEntry entry) {
            return new CompletionResult(CompletionStatus.ALREADY_COMPLETE, entry, Collections.<String>emptyList(), 0, 0, 0, 0);
        }

        public static CompletionResult missingRequirements(ResearchEntry entry, List<String> missingRequirements) {
            return new CompletionResult(CompletionStatus.MISSING_REQUIREMENTS, entry, missingRequirements, 0, 0, 0, 0);
        }

        public static CompletionResult missingSamples(ResearchEntry entry, int requiredSamples, int availableSamples) {
            return new CompletionResult(CompletionStatus.MISSING_SAMPLES, entry, Collections.<String>emptyList(), requiredSamples, availableSamples, 0, 0);
        }

        public static CompletionResult missingSupplies(ResearchEntry entry, int requiredSupplies, int availableSupplies) {
            return new CompletionResult(CompletionStatus.MISSING_SUPPLIES, entry, Collections.<String>emptyList(), 0, 0, requiredSupplies, availableSupplies);
        }

        public static CompletionResult completed(ResearchEntry entry) {
            return new CompletionResult(CompletionStatus.COMPLETED, entry, Collections.<String>emptyList(), 0, 0, 0, 0);
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

        public int getRequiredSupplies() {
            return requiredSupplies;
        }

        public int getAvailableSupplies() {
            return availableSupplies;
        }
    }

    public enum CompletionStatus {
        COMPLETED,
        ALREADY_COMPLETE,
        MISSING_REQUIREMENTS,
        MISSING_SAMPLES,
        MISSING_SUPPLIES,
        UNKNOWN_RESEARCH
    }
}
