package com.liamryan.standandhold.client;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.common.npc.HumanTier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import net.minecraft.resources.Identifier;

public final class HumanNpcRenderer extends HumanoidMobRenderer<HumanNpc, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    public static ModelLayerLocation layerId(HumanTier tier) { return new ModelLayerLocation(Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "human_" + tier.id), "main"); }
    private final Identifier texture;
    public HumanNpcRenderer(EntityRendererProvider.Context context, HumanTier tier) {
        super(context, new HumanoidModel<>(context.bakeLayer(layerId(tier))), 0.5F);
        texture = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "textures/entity/" + tier.id + ".png");
    }
    public static LayerDefinition layer(HumanTier tier) {
        return switch (tier) {
            case SURVIVOR_DEFENDER -> HumanNpcMeshes.survivor_defender();
            case ARMY_RIFLEMAN -> HumanNpcMeshes.army_rifleman();
        };
    }
    @Override public HumanoidRenderState createRenderState() { return new HumanoidRenderState(); }
    @Override protected HumanoidModel.ArmPose getArmPose(HumanNpc mob, HumanoidArm arm) {
        return mob.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && arm == mob.getMainArm()
                ? HumanoidModel.ArmPose.CROSSBOW_HOLD : super.getArmPose(mob, arm);
    }
    @Override public Identifier getTextureLocation(HumanoidRenderState state) { return texture; }
}
