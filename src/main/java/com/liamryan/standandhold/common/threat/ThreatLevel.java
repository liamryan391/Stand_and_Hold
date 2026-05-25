package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.config.StandAndHoldConfig;

public enum ThreatLevel {
    LOW(0, "Low"),
    GUARDED(1, "Guarded"),
    HIGH(2, "High"),
    CRITICAL(3, "Critical");

    private final int id;
    private final String displayName;

    ThreatLevel(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ThreatLevel fromScore(int score) {
        int[] thresholds = StandAndHoldConfig.threatResponse.threatLevelThresholds;
        if (thresholds == null || thresholds.length == 0) {
            thresholds = new int[] {
                    0,
                    10,
                    25,
                    50
            };
        }

        ThreatLevel result = LOW;
        for (ThreatLevel level : values()) {
            int thresholdIndex = Math.min(level.id, thresholds.length - 1);
            if (score >= Math.max(0, thresholds[thresholdIndex])) {
                result = level;
            }
        }
        return result;
    }
}
