package com.liamryan.standandhold.common.supply;

import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.world.HumanWorldData;

public final class SupplyManager {
    private SupplyManager() { }

    public static long add(HumanWorldData data, long amount) {
        requirePositive(amount);
        data.setSupplies(FoundationAmounts.credit(data.supplies(), amount));
        return data.supplies();
    }

    public static long spend(HumanWorldData data, long amount) {
        requirePositive(amount);
        data.setSupplies(FoundationAmounts.debit(data.supplies(), amount));
        return data.supplies();
    }

    private static void requirePositive(long amount) {
        if (amount <= 0) { throw new IllegalArgumentException("Supply amount must be positive."); }
    }
}
