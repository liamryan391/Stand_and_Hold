package com.liamryan.standandhold.client.render;

import com.liamryan.standandhold.common.entity.EntitySoldier;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    public static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntitySoldier.class, new IRenderFactory<EntitySoldier>() {
            @Override
            public Render<? super EntitySoldier> createRenderFor(RenderManager manager) {
                return new RenderSoldier(manager);
            }
        });
    }
}
