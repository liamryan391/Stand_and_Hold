package com.liamryan.standandhold.common.supply;

import net.minecraft.util.math.BlockPos;

public interface ISupplyStorage {
    int addStoredSupplies(int amount);

    int removeStoredSupplies(int amount);

    int getStoredSupplies();

    int getMaxStoredSupplies();

    BlockPos getPos();
}
