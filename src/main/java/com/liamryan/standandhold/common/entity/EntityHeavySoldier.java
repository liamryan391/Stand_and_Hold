package com.liamryan.standandhold.common.entity;

import net.minecraft.world.World;

public final class EntityHeavySoldier extends EntityHumanNpc {
    public EntityHeavySoldier(World world) {
        super(world);
    }

    @Override
    public HumanUnitTier getUnitTier() {
        return HumanUnitTier.HEAVY_SOLDIER;
    }
}
