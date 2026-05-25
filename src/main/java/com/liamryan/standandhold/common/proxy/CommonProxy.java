package com.liamryan.standandhold.common.proxy;

import com.liamryan.standandhold.common.network.PacketSyncHumanProgression;
import com.liamryan.standandhold.common.network.PacketSyncTileData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public Object getClientGuiElement(int id, EntityPlayer player, World world, BlockPos pos) {
        return null;
    }

    public void scheduleClientTask(Runnable task) {
    }

    public void handleHumanProgressionSync(PacketSyncHumanProgression message) {
    }

    public void handleTileDataSync(PacketSyncTileData message) {
    }
}
