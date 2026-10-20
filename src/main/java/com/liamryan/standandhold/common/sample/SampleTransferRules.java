package com.liamryan.standandhold.common.sample;

import com.liamryan.standandhold.common.building.BuildingState;

public final class SampleTransferRules {
    public static final int BATCH = 64;
    private SampleTransferRules() { }
    public static int deposit(int held, int stored) {
        check(held, stored);
        int amount = Math.min(BATCH, Math.min(held, BuildingState.MAX_SAMPLES - stored));
        if (amount == 0) { throw new IllegalArgumentException(held == 0 ? "No tissue samples in your inventory." : "Sample storage is full."); }
        return amount;
    }
    public static int withdraw(int room, int stored) {
        check(room, stored);
        int amount = Math.min(BATCH, Math.min(room, stored));
        if (amount == 0) { throw new IllegalArgumentException(stored == 0 ? "No samples stored." : "Inventory full; no samples removed."); }
        return amount;
    }
    private static void check(int amount, int stored) {
        if (amount < 0 || stored < 0 || stored > BuildingState.MAX_SAMPLES) { throw new IllegalArgumentException("Invalid sample transfer state."); }
    }
}
