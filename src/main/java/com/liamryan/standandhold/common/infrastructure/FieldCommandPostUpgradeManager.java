package com.liamryan.standandhold.common.infrastructure;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.util.ParasiteSampleHelper;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FieldCommandPostUpgradeManager {
    private static final List<FieldCommandPostUpgradeRequirement> DEFAULT_REQUIREMENTS = Collections.unmodifiableList(Arrays.asList(
            new FieldCommandPostUpgradeRequirement(FieldCommandPostLevel.REINFORCED_OUTPOST, 100, Arrays.asList("field_communications"), 1, 20, 25),
            new FieldCommandPostUpgradeRequirement(FieldCommandPostLevel.MILITARY_OUTPOST, 300, Arrays.asList("outpost_doctrine"), 2, 50, 50),
            new FieldCommandPostUpgradeRequirement(FieldCommandPostLevel.FORTIFIED_BASE, 700, Arrays.asList("outpost_doctrine", "parasite_samples"), 4, 100, 100),
            new FieldCommandPostUpgradeRequirement(FieldCommandPostLevel.MAIN_BASE, 1500, Arrays.asList("outpost_doctrine", "parasite_samples"), 8, 200, 200)
    ));

    private FieldCommandPostUpgradeManager() {
    }

    public static FieldCommandPostUpgradeRequirement getRequirementForLevel(FieldCommandPostLevel targetLevel) {
        if (targetLevel == null || targetLevel == FieldCommandPostLevel.FIELD_CAMP) {
            return null;
        }

        List<FieldCommandPostUpgradeRequirement> configuredRequirements = getConfiguredRequirements();
        for (FieldCommandPostUpgradeRequirement requirement : configuredRequirements) {
            if (requirement.getTargetLevel() == targetLevel) {
                return requirement;
            }
        }

        for (FieldCommandPostUpgradeRequirement requirement : DEFAULT_REQUIREMENTS) {
            if (requirement.getTargetLevel() == targetLevel) {
                return requirement;
            }
        }

        return null;
    }

    public static UpgradeResult tryUpgrade(World world, TileEntityFieldCommandPost commandPost, @Nullable EntityPlayer player) {
        FieldCommandPostLevel currentLevel = commandPost.getUpgradeLevelInfo();
        if (currentLevel.isMaxLevel()) {
            return UpgradeResult.alreadyMax(currentLevel);
        }

        FieldCommandPostLevel targetLevel = currentLevel.getNextLevel();
        FieldCommandPostUpgradeRequirement requirement = getRequirementForLevel(targetLevel);
        if (requirement == null) {
            return UpgradeResult.missingConfiguration(targetLevel);
        }

        HumanWorldData data = HumanPointManager.getData(world);
        int currentHumanPoints = data.getHumanPoints();
        if (currentHumanPoints < requirement.getRequiredHumanPoints()) {
            return UpgradeResult.missingPoints(requirement, currentHumanPoints);
        }

        List<String> missingResearchIds = getMissingResearchIds(data, requirement);
        if (!missingResearchIds.isEmpty()) {
            return UpgradeResult.missingResearch(requirement, missingResearchIds);
        }

        int availableSamples = ParasiteSampleHelper.getAvailableParasiteSamples(player);
        if (availableSamples < requirement.getParasiteSampleCost()) {
            return UpgradeResult.missingSamples(requirement, availableSamples);
        }

        int availableSupplies = SupplyManager.getSupplyPoints(world);
        if (availableSupplies < requirement.getSupplyCost()) {
            return UpgradeResult.missingSupplies(requirement, availableSupplies);
        }

        ParasiteSampleHelper.consumeParasiteSamples(player, requirement.getParasiteSampleCost());
        SupplyManager.spendSupplies(world, requirement.getSupplyCost(), "field command post upgrade to " + requirement.getTargetLevel().getDisplayName());
        commandPost.setUpgradeLevel(requirement.getTargetLevel().getLevel());
        if (requirement.getCompletionPointReward() > 0) {
            HumanPointManager.addPoints(world, requirement.getCompletionPointReward(), "field command post upgrade to " + requirement.getTargetLevel().getDisplayName());
        }

        StandAndHold.LOGGER.info("Field Command Post upgraded to Level {}: {}.", requirement.getTargetLevel().getLevel(), requirement.getTargetLevel().getDisplayName());
        return UpgradeResult.completed(requirement);
    }

    private static List<FieldCommandPostUpgradeRequirement> getConfiguredRequirements() {
        String[] configuredEntries = StandAndHoldConfig.infrastructure.fieldCommandPostUpgradeRequirements;
        if (configuredEntries == null || configuredEntries.length == 0) {
            return Collections.emptyList();
        }

        List<FieldCommandPostUpgradeRequirement> requirements = new ArrayList<FieldCommandPostUpgradeRequirement>();
        for (String configuredEntry : configuredEntries) {
            FieldCommandPostUpgradeRequirement requirement = parseRequirement(configuredEntry);
            if (requirement != null) {
                requirements.add(requirement);
            }
        }

        return requirements;
    }

    private static FieldCommandPostUpgradeRequirement parseRequirement(String configuredEntry) {
        if (configuredEntry == null || configuredEntry.trim().isEmpty()) {
            return null;
        }

        String[] parts = configuredEntry.split("\\|", -1);
        if (parts.length < 5) {
            StandAndHold.LOGGER.warn("Ignoring invalid Field Command Post upgrade requirement '{}'. Expected targetLevel|requiredHumanPoints|requiredResearchIds|parasiteSampleCost|supplyCost|pointReward.", configuredEntry);
            return null;
        }

        try {
            int rawTargetLevel = Integer.parseInt(parts[0].trim());
            if (rawTargetLevel < FieldCommandPostLevel.REINFORCED_OUTPOST.getLevel() || rawTargetLevel > FieldCommandPostLevel.MAIN_BASE.getLevel()) {
                return null;
            }
            FieldCommandPostLevel targetLevel = FieldCommandPostLevel.byLevel(rawTargetLevel);

            return new FieldCommandPostUpgradeRequirement(
                    targetLevel,
                    parseNonNegativeInt(parts[1]),
                    parseResearchIds(parts[2]),
                    parseNonNegativeInt(parts[3]),
                    parts.length >= 6 ? parseNonNegativeInt(parts[4]) : 0,
                    parseNonNegativeInt(parts.length >= 6 ? parts[5] : parts[4])
            );
        } catch (NumberFormatException exception) {
            StandAndHold.LOGGER.warn("Ignoring invalid Field Command Post upgrade requirement '{}': {}", configuredEntry, exception.getMessage());
            return null;
        }
    }

    private static List<String> parseResearchIds(String rawResearchIds) {
        if (rawResearchIds == null || rawResearchIds.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> researchIds = new ArrayList<String>();
        for (String rawResearchId : rawResearchIds.split(",")) {
            String normalizedId = ResearchEntry.normalizeId(rawResearchId);
            if (!normalizedId.isEmpty()) {
                researchIds.add(normalizedId);
            }
        }
        return researchIds;
    }

    private static List<String> getMissingResearchIds(HumanWorldData data, FieldCommandPostUpgradeRequirement requirement) {
        List<String> missingResearchIds = new ArrayList<String>();
        for (String researchId : requirement.getRequiredResearchIds()) {
            if (!data.isResearchCompleted(researchId)) {
                missingResearchIds.add(researchId);
            }
        }
        return missingResearchIds;
    }

    private static int parseNonNegativeInt(String rawValue) throws NumberFormatException {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0;
        }
        return Math.max(0, Integer.parseInt(rawValue.trim()));
    }

    public static final class UpgradeResult {
        private final UpgradeStatus status;
        private final FieldCommandPostUpgradeRequirement requirement;
        private final FieldCommandPostLevel level;
        private final List<String> missingResearchIds;
        private final int currentHumanPoints;
        private final int availableSamples;
        private final int availableSupplies;

        private UpgradeResult(UpgradeStatus status, FieldCommandPostUpgradeRequirement requirement, FieldCommandPostLevel level, List<String> missingResearchIds, int currentHumanPoints, int availableSamples, int availableSupplies) {
            this.status = status;
            this.requirement = requirement;
            this.level = level;
            this.missingResearchIds = missingResearchIds == null ? Collections.<String>emptyList() : Collections.unmodifiableList(missingResearchIds);
            this.currentHumanPoints = currentHumanPoints;
            this.availableSamples = availableSamples;
            this.availableSupplies = availableSupplies;
        }

        private static UpgradeResult completed(FieldCommandPostUpgradeRequirement requirement) {
            return new UpgradeResult(UpgradeStatus.COMPLETED, requirement, requirement.getTargetLevel(), Collections.<String>emptyList(), 0, 0, 0);
        }

        private static UpgradeResult alreadyMax(FieldCommandPostLevel level) {
            return new UpgradeResult(UpgradeStatus.ALREADY_MAX_LEVEL, null, level, Collections.<String>emptyList(), 0, 0, 0);
        }

        private static UpgradeResult missingConfiguration(FieldCommandPostLevel level) {
            return new UpgradeResult(UpgradeStatus.MISSING_CONFIGURATION, null, level, Collections.<String>emptyList(), 0, 0, 0);
        }

        private static UpgradeResult missingPoints(FieldCommandPostUpgradeRequirement requirement, int currentHumanPoints) {
            return new UpgradeResult(UpgradeStatus.MISSING_POINTS, requirement, requirement.getTargetLevel(), Collections.<String>emptyList(), currentHumanPoints, 0, 0);
        }

        private static UpgradeResult missingResearch(FieldCommandPostUpgradeRequirement requirement, List<String> missingResearchIds) {
            return new UpgradeResult(UpgradeStatus.MISSING_RESEARCH, requirement, requirement.getTargetLevel(), missingResearchIds, 0, 0, 0);
        }

        private static UpgradeResult missingSamples(FieldCommandPostUpgradeRequirement requirement, int availableSamples) {
            return new UpgradeResult(UpgradeStatus.MISSING_SAMPLES, requirement, requirement.getTargetLevel(), Collections.<String>emptyList(), 0, availableSamples, 0);
        }

        private static UpgradeResult missingSupplies(FieldCommandPostUpgradeRequirement requirement, int availableSupplies) {
            return new UpgradeResult(UpgradeStatus.MISSING_SUPPLIES, requirement, requirement.getTargetLevel(), Collections.<String>emptyList(), 0, 0, availableSupplies);
        }

        public UpgradeStatus getStatus() {
            return status;
        }

        public FieldCommandPostUpgradeRequirement getRequirement() {
            return requirement;
        }

        public FieldCommandPostLevel getLevel() {
            return level;
        }

        public List<String> getMissingResearchIds() {
            return missingResearchIds;
        }

        public int getCurrentHumanPoints() {
            return currentHumanPoints;
        }

        public int getAvailableSamples() {
            return availableSamples;
        }

        public int getAvailableSupplies() {
            return availableSupplies;
        }
    }

    public enum UpgradeStatus {
        COMPLETED,
        ALREADY_MAX_LEVEL,
        MISSING_CONFIGURATION,
        MISSING_POINTS,
        MISSING_RESEARCH,
        MISSING_SAMPLES,
        MISSING_SUPPLIES
    }
}
