package com.liamryan.standandhold.common.mission;

/** Completed is also the permanent reward receipt; completed missions cannot be reset. */
public record MissionState(long progress, boolean completed) {
    public MissionState {
        if (progress < 0) { throw new IllegalArgumentException("Mission progress cannot be negative."); }
    }
}
