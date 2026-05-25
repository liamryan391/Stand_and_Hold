package com.liamryan.standandhold.client.render;

import com.liamryan.standandhold.common.entity.EntityEliteSoldier;
import com.liamryan.standandhold.common.entity.EntityHeavySoldier;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.EntitySoldier;
import com.liamryan.standandhold.common.entity.EntitySpecialParasiteDivisionOperative;
import com.liamryan.standandhold.common.entity.EntitySuperEliteSoldier;
import com.liamryan.standandhold.common.entity.EntitySurvivorDefender;
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
        registerHumanUnitRenderer(EntitySurvivorDefender.class);
        registerHumanUnitRenderer(EntitySoldier.class);
        registerHumanUnitRenderer(EntityHeavySoldier.class);
        registerHumanUnitRenderer(EntityEliteSoldier.class);
        registerHumanUnitRenderer(EntitySuperEliteSoldier.class);
        registerHumanUnitRenderer(EntitySpecialParasiteDivisionOperative.class);
    }

    private static <T extends EntityHumanNpc> void registerHumanUnitRenderer(Class<T> entityClass) {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, new IRenderFactory<T>() {
            @Override
            public Render<? super T> createRenderFor(RenderManager manager) {
                return new RenderHumanNpc<T>(manager);
            }
        });
    }
}
