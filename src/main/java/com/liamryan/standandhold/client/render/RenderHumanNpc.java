package com.liamryan.standandhold.client.render;

import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderHumanNpc<T extends EntityHumanNpc> extends RenderBiped<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/steve.png");

    public RenderHumanNpc(RenderManager renderManager) {
        super(renderManager, new ModelBiped(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return TEXTURE;
    }
}
