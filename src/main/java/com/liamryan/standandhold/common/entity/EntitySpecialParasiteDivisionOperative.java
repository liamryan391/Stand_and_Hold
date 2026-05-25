package com.liamryan.standandhold.common.entity;

import net.minecraft.world.World;

public final class EntitySpecialParasiteDivisionOperative extends EntityHumanNpc {
    public EntitySpecialParasiteDivisionOperative(World world) {
        super(world);
    }

    @Override
    public HumanUnitTier getUnitTier() {
        return HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE;
    }
}
