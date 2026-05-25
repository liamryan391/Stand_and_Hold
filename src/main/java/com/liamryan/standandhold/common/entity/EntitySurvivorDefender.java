package com.liamryan.standandhold.common.entity;

import net.minecraft.world.World;

public final class EntitySurvivorDefender extends EntityHumanNpc {
    public EntitySurvivorDefender(World world) {
        super(world);
    }

    @Override
    public HumanUnitTier getUnitTier() {
        return HumanUnitTier.SURVIVOR_DEFENDER;
    }
}
