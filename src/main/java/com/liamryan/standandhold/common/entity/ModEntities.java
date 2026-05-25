package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {
    private static int nextEntityId = 1;

    private ModEntities() {
    }

    public static void registerEntities() {
        registerUnit(HumanUnitTier.SURVIVOR_DEFENDER, EntitySurvivorDefender.class);
        registerUnit(HumanUnitTier.ARMY_RIFLEMAN, EntitySoldier.class);
        registerUnit(HumanUnitTier.HEAVY_SOLDIER, EntityHeavySoldier.class);
        registerUnit(HumanUnitTier.ELITE_SOLDIER, EntityEliteSoldier.class);
        registerUnit(HumanUnitTier.SUPER_ELITE_SOLDIER, EntitySuperEliteSoldier.class);
        registerUnit(HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE, EntitySpecialParasiteDivisionOperative.class);
    }

    private static void registerUnit(HumanUnitTier tier, Class<? extends EntityHumanNpc> entityClass) {
        EntityRegistry.registerModEntity(
                new ResourceLocation(StandAndHoldConstants.MOD_ID, tier.getRegistryName()),
                entityClass,
                tier.getRegistryName(),
                nextEntityId++,
                StandAndHold.instance,
                64,
                3,
                true,
                tier.getEggPrimaryColor(),
                tier.getEggSecondaryColor()
        );
    }
}
