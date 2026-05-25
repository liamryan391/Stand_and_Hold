package com.liamryan.standandhold.client;

import com.liamryan.standandhold.client.render.ModEntityRenderers;
import com.liamryan.standandhold.common.proxy.CommonProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ModEntityRenderers.registerEntityRenderers();
    }
}
