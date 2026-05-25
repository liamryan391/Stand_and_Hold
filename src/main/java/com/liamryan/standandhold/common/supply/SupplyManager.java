package com.liamryan.standandhold.common.supply;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.world.World;

public final class SupplyManager {
    private SupplyManager() {
    }

    public static int getSupplyPoints(World world) {
        if (world == null) {
            return 0;
        }
        return HumanPointManager.getData(world).getSupplyPoints();
    }

    public static int addSupplies(World world, int amount, String source) {
        if (world == null || world.isRemote || amount <= 0) {
            return getSupplyPoints(world);
        }

        HumanWorldData data = HumanPointManager.getData(world);
        data.addSupplyPoints(amount);
        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Added {} supply points from {}. Total: {}.", amount, source, data.getSupplyPoints());
        }
        return data.getSupplyPoints();
    }

    public static boolean hasSupplies(World world, int amount) {
        return amount <= 0 || getSupplyPoints(world) >= amount;
    }

    public static boolean spendSupplies(World world, int amount, String source) {
        if (world == null || world.isRemote || amount <= 0) {
            return true;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        if (!data.consumeSupplyPoints(amount)) {
            return false;
        }

        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Spent {} supply points on {}. Remaining: {}.", amount, source, data.getSupplyPoints());
        }
        return true;
    }

    public static int getSupplyCrateValue() {
        return Math.max(1, StandAndHoldConfig.supply.supplyCrateValue);
    }
}
