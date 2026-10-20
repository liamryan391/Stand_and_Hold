package com.liamryan.standandhold.client;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.common.npc.HumanTier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public final class HumanNpcRenderer extends HumanoidMobRenderer<HumanNpc, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "human"), "main");
    private final Identifier texture;
    public HumanNpcRenderer(EntityRendererProvider.Context context, HumanTier tier) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.5F);
        texture = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "textures/entity/" + tier.id + ".png");
    }
    public static LayerDefinition layer() { return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 32); }
    @Override public HumanoidRenderState createRenderState() { return new HumanoidRenderState(); }
    @Override public Identifier getTextureLocation(HumanoidRenderState state) { return texture; }
}
