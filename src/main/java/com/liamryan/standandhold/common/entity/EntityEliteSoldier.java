package com.liamryan.standandhold.common.entity;

import net.minecraft.world.World;

public final class EntityEliteSoldier extends EntityHumanNpc {
    public EntityEliteSoldier(World world) {
        super(world);
    }

    @Override
    public HumanUnitTier getUnitTier() {
        return HumanUnitTier.ELITE_SOLDIER;
    }
}
