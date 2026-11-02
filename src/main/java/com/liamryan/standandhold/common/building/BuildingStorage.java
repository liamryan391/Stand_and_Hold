package com.liamryan.standandhold.common.building;

import java.util.List;

/** Derived limits never rewrite saved balances or add resources. */
public final class BuildingStorage {
    public static final int MAX_CAPACITY = 1_000_000;
    public static final List<Integer> DEFAULT_LEVELS = List.of(2000, 3000, 4000, 6000, 8000);
    private BuildingStorage() { }

    public static boolean validProfile(Object raw) {
        if (!(raw instanceof List<?> values) || values.size() != 5) { return false; }
        long previous = 0;
        for (Object value : values) {
            if (!(value instanceof Number n) || n.doubleValue() != n.longValue() || n.longValue() < 1
                    || n.longValue() > MAX_CAPACITY || n.longValue() < previous) { return false; }
            previous = n.longValue();
        }
        return true;
    }

    public static int capacity(BuildingKind kind, int level, int flat, boolean levelled, Object profile) {
        if (kind == null || level < 1 || level > 5 || flat < 1 || flat > MAX_CAPACITY) {
            throw new IllegalArgumentException("Invalid building supply capacity settings.");
        }
        if (kind != BuildingKind.COMMAND_POST || !levelled || !validProfile(profile)) { return flat; }
        return ((Number) ((List<?>) profile).get(level - 1)).intValue();
    }

    public static BuildingState generate(BuildingState state, int reward, long capacity) {
        if (reward < 0 || capacity < 1 || capacity > MAX_CAPACITY) { throw new IllegalArgumentException("Invalid supply generation limit."); }
        if (state.supplies() >= capacity || reward == 0) { return state; }
        return state.supplies(state.supplies() + Math.min((long) reward, capacity - state.supplies()));
    }
}
