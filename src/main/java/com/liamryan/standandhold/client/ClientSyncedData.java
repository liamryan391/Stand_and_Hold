package com.liamryan.standandhold.client;

import com.liamryan.standandhold.common.network.PacketSyncTileData;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public final class ClientSyncedData {
    private static final Map<Long, TileSnapshot> TILE_DATA = new HashMap<Long, TileSnapshot>();

    private static int humanPoints = -1;
    private static int stageId = -1;
    private static int supplyPoints = -1;

    private ClientSyncedData() {
    }

    public static void setProgression(int syncedHumanPoints, int syncedStageId, int syncedSupplyPoints) {
        humanPoints = syncedHumanPoints;
        stageId = syncedStageId;
        supplyPoints = syncedSupplyPoints;
    }

    public static int getHumanPoints(int fallback) {
        return humanPoints >= 0 ? humanPoints : fallback;
    }

    public static int getStageId(int fallback) {
        return stageId >= 0 ? stageId : fallback;
    }

    public static int getSupplyPoints(int fallback) {
        return supplyPoints >= 0 ? supplyPoints : fallback;
    }

    public static void setTileData(PacketSyncTileData message) {
        TILE_DATA.put(Long.valueOf(message.getPos().toLong()), TileSnapshot.from(message));
    }

    public static TileSnapshot getTileData(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return TILE_DATA.get(Long.valueOf(pos.toLong()));
    }

    public static final class TileSnapshot {
        private final int typeId;
        private final int upgradeLevel;
        private final int storedSupplies;
        private final int maxStoredSupplies;
        private final int researchProgress;
        private final int researchProgressRequired;
        private final int storedSamples;
        private final int maxStoredSamples;
        private final int targetSampleCost;
        private final int targetSupplyCost;
        private final String targetResearchId;
        private final String targetResearchLabel;

        private TileSnapshot(int typeId, int upgradeLevel, int storedSupplies, int maxStoredSupplies, int researchProgress, int researchProgressRequired, int storedSamples, int maxStoredSamples, int targetSampleCost, int targetSupplyCost, String targetResearchId, String targetResearchLabel) {
            this.typeId = typeId;
            this.upgradeLevel = upgradeLevel;
            this.storedSupplies = storedSupplies;
            this.maxStoredSupplies = maxStoredSupplies;
            this.researchProgress = researchProgress;
            this.researchProgressRequired = researchProgressRequired;
            this.storedSamples = storedSamples;
            this.maxStoredSamples = maxStoredSamples;
            this.targetSampleCost = targetSampleCost;
            this.targetSupplyCost = targetSupplyCost;
            this.targetResearchId = targetResearchId;
            this.targetResearchLabel = targetResearchLabel;
        }

        private static TileSnapshot from(PacketSyncTileData message) {
            return new TileSnapshot(
                    message.getTypeId(),
                    message.getUpgradeLevel(),
                    message.getStoredSupplies(),
                    message.getMaxStoredSupplies(),
                    message.getResearchProgress(),
                    message.getResearchProgressRequired(),
                    message.getStoredSamples(),
                    message.getMaxStoredSamples(),
                    message.getTargetSampleCost(),
                    message.getTargetSupplyCost(),
                    message.getTargetResearchId(),
                    message.getTargetResearchLabel()
            );
        }

        public int getTypeId() {
            return typeId;
        }

        public int getUpgradeLevel() {
            return upgradeLevel;
        }

        public int getStoredSupplies() {
            return storedSupplies;
        }

        public int getMaxStoredSupplies() {
            return maxStoredSupplies;
        }

        public int getResearchProgress() {
            return researchProgress;
        }

        public int getResearchProgressRequired() {
            return researchProgressRequired;
        }

        public int getStoredSamples() {
            return storedSamples;
        }

        public int getMaxStoredSamples() {
            return maxStoredSamples;
        }

        public int getTargetSampleCost() {
            return targetSampleCost;
        }

        public int getTargetSupplyCost() {
            return targetSupplyCost;
        }

        public String getTargetResearchId() {
            return targetResearchId;
        }

        public String getTargetResearchLabel() {
            return targetResearchLabel;
        }
    }
}
