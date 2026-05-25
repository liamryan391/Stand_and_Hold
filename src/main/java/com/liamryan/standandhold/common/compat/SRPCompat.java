package com.liamryan.standandhold.common.compat;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.util.Locale;

public final class SRPCompat {
    private static final String[] VERIFIED_DEFAULT_MAPPINGS = new String[0];

    private SRPCompat() {
    }

    public static boolean isLoaded() {
        if (!StandAndHoldConfig.compatibility.enableScapeAndRunParasitesCompatibility) {
            return false;
        }

        String modId = getConfiguredModId();
        return !modId.isEmpty() && Loader.isModLoaded(modId);
    }

    public static String getConfiguredModId() {
        String modId = StandAndHoldConfig.compatibility.scapeAndRunParasitesModId;
        return modId == null ? "" : modId.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean hasConfiguredMappings() {
        String[] mappings = StandAndHoldConfig.compatibility.srpParasiteMappings;
        if (mappings != null && mappings.length > 0) {
            return true;
        }
        return StandAndHoldConfig.compatibility.enableVerifiedSrpDefaultMappings && VERIFIED_DEFAULT_MAPPINGS.length > 0;
    }

    public static int getConfiguredMappingCount() {
        int count = 0;
        String[] mappings = StandAndHoldConfig.compatibility.srpParasiteMappings;
        if (mappings != null) {
            for (String mapping : mappings) {
                if (parseMapping(mapping) != null) {
                    count++;
                }
            }
        }

        if (StandAndHoldConfig.compatibility.enableVerifiedSrpDefaultMappings) {
            for (String mapping : VERIFIED_DEFAULT_MAPPINGS) {
                if (parseMapping(mapping) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean isMappedParasiteEntity(ResourceLocation entityRegistryId) {
        if (!isLoaded()) {
            return false;
        }

        ParsedSrpMapping mapping = getMapping(entityRegistryId);
        return mapping != null && (mapping.killReward > 0 || mapping.sampleDropChance > 0.0D || mapping.humanTarget);
    }

    public static boolean isMappedHumanTargetEntity(ResourceLocation entityRegistryId) {
        if (!isLoaded()) {
            return false;
        }

        ParsedSrpMapping mapping = getMapping(entityRegistryId);
        return mapping != null && mapping.humanTarget;
    }

    public static int getKillReward(ResourceLocation entityRegistryId) {
        if (!isLoaded()) {
            return 0;
        }

        ParsedSrpMapping mapping = getMapping(entityRegistryId);
        return mapping == null ? 0 : mapping.killReward;
    }

    public static double getSampleDropChance(ResourceLocation entityRegistryId) {
        if (!isLoaded()) {
            return 0.0D;
        }

        ParsedSrpMapping mapping = getMapping(entityRegistryId);
        return mapping == null ? 0.0D : mapping.sampleDropChance;
    }

    private static ParsedSrpMapping getMapping(ResourceLocation entityRegistryId) {
        if (entityRegistryId == null) {
            return null;
        }

        String entityId = entityRegistryId.toString().toLowerCase(Locale.ROOT);
        ParsedSrpMapping mapping = getMapping(entityId, StandAndHoldConfig.compatibility.srpParasiteMappings);
        if (mapping != null) {
            return mapping;
        }

        return StandAndHoldConfig.compatibility.enableVerifiedSrpDefaultMappings ? getMapping(entityId, VERIFIED_DEFAULT_MAPPINGS) : null;
    }

    private static ParsedSrpMapping getMapping(String entityId, String[] mappings) {
        if (mappings == null) {
            return null;
        }

        for (String mappingEntry : mappings) {
            ParsedSrpMapping mapping = parseMapping(mappingEntry);
            if (mapping != null && mapping.entityId.equals(entityId)) {
                return mapping;
            }
        }
        return null;
    }

    private static ParsedSrpMapping parseMapping(String mappingEntry) {
        if (mappingEntry == null || mappingEntry.trim().isEmpty()) {
            return null;
        }

        String[] parts = mappingEntry.split("\\|", -1);
        if (parts.length < 1) {
            return null;
        }

        String entityId = normalizeEntityId(parts[0]);
        if (entityId == null) {
            return null;
        }

        int killReward = parts.length >= 2 ? parseNonNegativeInt(parts[1]) : 0;
        double sampleDropChance = parts.length >= 3 ? parseChance(parts[2]) : 0.0D;
        boolean humanTarget = parts.length < 4 || parseBoolean(parts[3], true);
        return new ParsedSrpMapping(entityId, killReward, sampleDropChance, humanTarget);
    }

    private static String normalizeEntityId(String rawEntityId) {
        if (rawEntityId == null || rawEntityId.trim().isEmpty()) {
            return null;
        }

        try {
            return new ResourceLocation(rawEntityId.trim().toLowerCase(Locale.ROOT)).toString();
        } catch (RuntimeException ignored) {
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

    private static double parseChance(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return 0.0D;
        }

        try {
            return Math.max(0.0D, Math.min(1.0D, Double.parseDouble(rawValue.trim())));
        } catch (NumberFormatException ignored) {
            return 0.0D;
        }
    }

    private static boolean parseBoolean(String rawValue, boolean fallback) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return fallback;
        }

        String value = rawValue.trim().toLowerCase(Locale.ROOT);
        return "true".equals(value) || "yes".equals(value) || "1".equals(value) || "target".equals(value);
    }

    private static final class ParsedSrpMapping {
        private final String entityId;
        private final int killReward;
        private final double sampleDropChance;
        private final boolean humanTarget;

        private ParsedSrpMapping(String entityId, int killReward, double sampleDropChance, boolean humanTarget) {
            this.entityId = entityId;
            this.killReward = Math.max(0, killReward);
            this.sampleDropChance = Math.max(0.0D, Math.min(1.0D, sampleDropChance));
            this.humanTarget = humanTarget;
        }
    }
}
