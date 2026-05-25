package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import javax.annotation.Nullable;

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

    @Nullable
    public static EntityHumanNpc createHumanUnit(World world, HumanUnitTier tier) {
        if (world == null || tier == null) {
            return null;
        }

        switch (tier) {
            case SURVIVOR_DEFENDER:
                return new EntitySurvivorDefender(world);
            case ARMY_RIFLEMAN:
                return new EntitySoldier(world);
            case HEAVY_SOLDIER:
                return new EntityHeavySoldier(world);
            case ELITE_SOLDIER:
                return new EntityEliteSoldier(world);
            case SUPER_ELITE_SOLDIER:
                return new EntitySuperEliteSoldier(world);
            case SPECIAL_PARASITE_DIVISION_OPERATIVE:
                return new EntitySpecialParasiteDivisionOperative(world);
            default:
                return null;
        }
    }
}
