package com.liamryan.standandhold.common.proxy;

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
}
