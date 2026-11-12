package com.liamryan.standandhold.client;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.common.npc.HumanTier;
import com.liamryan.standandhold.common.npc.NpcVariants;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public final class HumanNpcRenderer extends HumanoidMobRenderer<HumanNpc, HumanNpcRenderer.State, HumanoidModel<HumanNpcRenderer.State>> {
    public static final class State extends HumanoidRenderState {
        boolean launcherMotion;
        float launcherAim, launcherKick;
        int variant;
        float variantHeight = 1, variantWidth = 1;
    }
    public static ModelLayerLocation layerId(HumanTier tier) { return new ModelLayerLocation(Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "human_" + tier.id), "main"); }
    private final Identifier[] textures = new Identifier[NpcVariants.COUNT];
    public HumanNpcRenderer(EntityRendererProvider.Context context, HumanTier tier) {
        super(context, new MotionModel(context.bakeLayer(layerId(tier))), 0.5F);
        for (int i = 0; i < textures.length; i++) {
            textures[i] = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "textures/entity/variants/" + tier.id + "_" + i + ".png");
        }
    }
    public static LayerDefinition layer(HumanTier tier) {
        return switch (tier) {
            case SURVIVOR_DEFENDER -> HumanNpcMeshes.survivor_defender();
            case ARMY_RIFLEMAN -> HumanNpcMeshes.army_rifleman();
            case HEAVY_SOLDIER -> HumanNpcMeshes.heavy_soldier();
            case ELITE_SOLDIER -> HumanNpcMeshes.elite_soldier();
            case SUPER_ELITE -> HumanNpcMeshes.super_elite();
            case SPECIAL_DIVISION -> HumanNpcMeshes.special_division_operative();
        };
    }
    @Override public State createRenderState() { return new State(); }
    @Override protected HumanoidModel.ArmPose getArmPose(HumanNpc mob, HumanoidArm arm) {
        return mob.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && arm == mob.getMainArm()
                ? mob.tier().usesLauncherMotion() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.CROSSBOW_HOLD
                : super.getArmPose(mob, arm);
    }
    @Override public void extractRenderState(HumanNpc mob, State state, float partialTick) {
        super.extractRenderState(mob, state, partialTick);
        state.launcherMotion = mob.tier().usesLauncherMotion() && mob.isAlive()
                && mob.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && mob.getOffhandItem().isEmpty();
        state.launcherAim = state.launcherMotion ? mob.launcherAim(partialTick) : 0;
        state.launcherKick = state.launcherMotion ? mob.launcherKick(partialTick) : 0;
        state.variant = NpcVariants.index(mob.getUUID());
        state.variantHeight = NpcVariants.height(mob.getUUID());
        state.variantWidth = NpcVariants.width(mob.getUUID());
    }
    @Override public Identifier getTextureLocation(State state) { return textures[Math.floorMod(state.variant, textures.length)]; }
    /** Render-only build variation around the feet; the hitbox is unchanged. */
    @Override protected void scale(State state, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        poseStack.scale(state.variantWidth, state.variantHeight, state.variantWidth);
    }

    private static final class MotionModel extends HumanoidModel<State> {
        MotionModel(ModelPart root) { super(root); }
        @Override public void setupAnim(State state) {
            super.setupAnim(state);
            if (!state.launcherMotion || state.swingAnimation > 0 || state.isUsingItem
                    || state.isPassenger || state.swimAmount > 0 || state.isFallFlying) { return; }
            boolean right = state.mainArm == HumanoidArm.RIGHT;
            var main = right ? rightArm : leftArm;
            var support = right ? leftArm : rightArm;
            float side = right ? 1 : -1;
            float carryMain = -0.62F + main.xRot * 0.2F;
            float carrySupport = -0.38F + support.xRot * 0.24F;
            float blend = Math.max(state.launcherAim, state.launcherKick);
            AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, right);
            main.xRot = Mth.lerp(blend, carryMain, main.xRot) - 0.13F * state.launcherKick;
            main.yRot = Mth.lerp(blend, -0.12F * side, main.yRot);
            support.xRot = Mth.lerp(blend, carrySupport, support.xRot) - 0.07F * state.launcherKick;
            support.yRot = Mth.lerp(blend, 0.2F * side, support.yRot);
        }
    }
}
