package com.liamryan.standandhold.common.progression;

import java.util.List;

public final class ProgressionRules {
    public static final List<Long> DEFAULT_THRESHOLDS = List.of(0L, 150L, 500L, 1200L, 2600L, 5200L, 10000L);

    private ProgressionRules() {
    }

    public static boolean validThresholds(Object candidate) {
        if (!(candidate instanceof List<?> values) || values.size() != HumanStage.values().length) {
            return false;
        }
        long previous = -1;
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) {
                return false;
            }
            long threshold = ((Number) value).longValue();
            if ((i == 0 && threshold != 0) || threshold <= previous) {
                return false;
            }
            previous = threshold;
        }
        return true;
    }

    public static long[] thresholds(List<? extends Number> values) {
        if (!validThresholds(values)) {
            throw new IllegalArgumentException("Stage thresholds must be seven increasing whole numbers starting at zero.");
        }
        return values.stream().mapToLong(Number::longValue).toArray();
    }

    public static HumanStage stageFor(long points, long[] thresholds) {
        if (points < 0 || thresholds.length != HumanStage.values().length || thresholds[0] != 0) {
            throw new IllegalArgumentException("Invalid progression points or thresholds.");
        }
        for (int i = 1; i < thresholds.length; i++) {
            if (thresholds[i] <= thresholds[i - 1]) {
                throw new IllegalArgumentException("Stage thresholds must be strictly increasing.");
            }
        }
        for (int level = thresholds.length - 1; level >= 0; level--) {
            if (points >= thresholds[level]) {
                return HumanStage.fromLevel(level);
            }
        }
        return HumanStage.SURVIVORS;
    }

    public static long addExact(long points, long amount) {
        if (points < 0 || amount <= 0) {
            throw new IllegalArgumentException("Point amount must be positive; saved points cannot be negative.");
        }
        if (amount > Long.MAX_VALUE - points) {
            throw new IllegalArgumentException("Adding that amount would exceed the maximum human point total.");
        }
        return points + amount;
    }
}
