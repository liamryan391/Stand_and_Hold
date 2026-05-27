package com.liamryan.standandhold.client.render;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderHumanNpc<T extends EntityHumanNpc> extends RenderBiped<T> {
    private static final ResourceLocation SURVIVOR_DEFENDER_TEXTURE = texture(HumanUnitTier.SURVIVOR_DEFENDER);
    private static final ResourceLocation ARMY_RIFLEMAN_TEXTURE = texture(HumanUnitTier.ARMY_RIFLEMAN);
    private static final ResourceLocation HEAVY_SOLDIER_TEXTURE = texture(HumanUnitTier.HEAVY_SOLDIER);
    private static final ResourceLocation ELITE_SOLDIER_TEXTURE = texture(HumanUnitTier.ELITE_SOLDIER);
    private static final ResourceLocation SUPER_ELITE_SOLDIER_TEXTURE = texture(HumanUnitTier.SUPER_ELITE_SOLDIER);
    private static final ResourceLocation SPECIAL_PARASITE_DIVISION_TEXTURE = texture(HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE);

    public RenderHumanNpc(RenderManager renderManager) {
        super(renderManager, new ModelBiped(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        HumanUnitTier tier = entity == null ? HumanUnitTier.SURVIVOR_DEFENDER : entity.getUnitTier();
        switch (tier) {
            case ARMY_RIFLEMAN:
                return ARMY_RIFLEMAN_TEXTURE;
            case HEAVY_SOLDIER:
                return HEAVY_SOLDIER_TEXTURE;
            case ELITE_SOLDIER:
                return ELITE_SOLDIER_TEXTURE;
            case SUPER_ELITE_SOLDIER:
                return SUPER_ELITE_SOLDIER_TEXTURE;
            case SPECIAL_PARASITE_DIVISION_OPERATIVE:
                return SPECIAL_PARASITE_DIVISION_TEXTURE;
            case SURVIVOR_DEFENDER:
            default:
                return SURVIVOR_DEFENDER_TEXTURE;
        }
    }

    private static ResourceLocation texture(HumanUnitTier tier) {
        return new ResourceLocation(StandAndHoldConstants.MOD_ID, "textures/entity/human/" + tier.getId() + ".png");
    }
}
