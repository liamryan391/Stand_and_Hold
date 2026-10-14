package com.liamryan.standandhold.common.foundation;

public record FoundationReward(long points, long supplies) {
    public FoundationReward {
        if (points < 0 || supplies < 0) {
            throw new IllegalArgumentException("Rewards cannot be negative.");
        }
    }
}
