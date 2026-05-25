package com.liamryan.standandhold.common.supply;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.world.World;

public final class LogisticsRouteManager {
    private LogisticsRouteManager() {
    }

    public static void updateCommandPostRoute(TileEntityFieldCommandPost commandPost) {
        if (commandPost == null || !StandAndHoldConfig.supply.enableCommandPostLogisticsRoute) {
            return;
        }

        World world = commandPost.getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        int interval = Math.max(1, StandAndHoldConfig.supply.commandPostLogisticsRouteInterval);
        long worldTime = world.getTotalWorldTime();
        long lastRouteTime = commandPost.getLastLogisticsRouteTime();
        if (lastRouteTime < 0L) {
            commandPost.setLastLogisticsRouteTime(worldTime);
            return;
        }

        if (worldTime - lastRouteTime < interval) {
            return;
        }

        commandPost.setLastLogisticsRouteTime(worldTime);
        int minimumLocalSupplies = Math.max(0, StandAndHoldConfig.supply.commandPostLogisticsRouteMinimumLocalSupplies);
        if (commandPost.getStoredSupplies() < minimumLocalSupplies) {
            return;
        }

        SupplyTransferManager.TransferResult result = SupplyTransferManager.exportLocalSupplies(
                world,
                commandPost,
                Math.max(1, StandAndHoldConfig.supply.commandPostLogisticsRouteTransferAmount),
                "command post logistics route export: " + commandPost.getPos()
        );

        if (result.getMovedSupplies() > 0 && StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Command Post logistics route at {} exported {} supplies. Global supplies: {}.",
                    commandPost.getPos(),
                    result.getMovedSupplies(),
                    result.getGlobalSupplies());
        }
    }
}
