package com.liamryan.standandhold.common.entity;

import net.minecraft.world.World;

public final class EntitySuperEliteSoldier extends EntityHumanNpc {
    public EntitySuperEliteSoldier(World world) {
        super(world);
    }

    @Override
    public HumanUnitTier getUnitTier() {
        return HumanUnitTier.SUPER_ELITE_SOLDIER;
    }
}
