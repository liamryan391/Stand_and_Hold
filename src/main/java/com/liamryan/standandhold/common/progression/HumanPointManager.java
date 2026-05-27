package com.liamryan.standandhold.common.progression;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import com.liamryan.standandhold.common.world.HumanWorldData;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;

public final class HumanPointManager {
    private HumanPointManager() {
    }

    public static HumanWorldData getData(World world) {
        WorldServer storageWorld = getStorageWorld(world);
        MapStorage storage = storageWorld.getMapStorage();
        HumanWorldData data = (HumanWorldData) storage.getOrLoadData(HumanWorldData.class, HumanWorldData.DATA_NAME);

        if (data == null) {
            data = new HumanWorldData(HumanWorldData.DATA_NAME);
            storage.setData(HumanWorldData.DATA_NAME, data);
            data.markDirty();
        }

        return data;
    }

    public static int addPoints(World world, int amount, String reason) {
        if (amount <= 0) {
            return getData(world).getHumanPoints();
        }

        HumanWorldData data = getData(world);
        data.addHumanPoints(amount);
        HumanStage previousStage = data.getStage();
        HumanStage newStage = recalculateStage(data);

        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Added {} human points for {}. Total is now {}.", amount, reason, data.getHumanPoints());
        }

        if (previousStage != newStage) {
            StandAndHold.LOGGER.info("Human stage advanced to Stage {}: {}.", newStage.getId(), newStage.getDisplayName());
        }
        MissionManager.recordHumanStageReached(world, null);

        return data.getHumanPoints();
    }

    public static HumanStage setStage(World world, HumanStage stage) {
        HumanWorldData data = getData(world);
        data.setStage(stage);
        StandAndHold.LOGGER.info("Human stage manually set to Stage {}: {}.", data.getStage().getId(), data.getStage().getDisplayName());
        MissionManager.recordHumanStageReached(world, null);
        return data.getStage();
    }

    public static HumanStage getStageForPoints(int points) {
        HumanStage result = HumanStage.SURVIVORS;
        for (HumanStage stage : HumanStage.values()) {
            if (points >= StandAndHoldConfig.getStageThreshold(stage.getId())) {
                result = stage;
            }
        }
        return result;
    }

    public static HumanStage recalculateStage(World world) {
        return recalculateStage(getData(world));
    }

    private static HumanStage recalculateStage(HumanWorldData data) {
        HumanStage stage = getStageForPoints(data.getHumanPoints());
        data.setStage(stage);
        return stage;
    }

    private static WorldServer getStorageWorld(World world) {
        if (world == null || world.getMinecraftServer() == null) {
            throw new IllegalArgumentException("Human progression data requires a server world.");
        }

        WorldServer overworld = world.getMinecraftServer().getWorld(0);
        if (overworld == null) {
            throw new IllegalStateException("Could not find overworld for human progression data.");
        }

        return overworld;
    }
}
