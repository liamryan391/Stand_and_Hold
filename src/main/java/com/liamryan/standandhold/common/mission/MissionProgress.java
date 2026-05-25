package com.liamryan.standandhold.common.mission;

import net.minecraft.nbt.NBTTagCompound;

public final class MissionProgress {
    private static final String TAG_ID = "Id";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_COMPLETED = "Completed";
    private static final String TAG_STARTED_WORLD_TIME = "StartedWorldTime";
    private static final String TAG_COMPLETED_WORLD_TIME = "CompletedWorldTime";

    private final String missionId;
    private int progress;
    private boolean completed;
    private long startedWorldTime;
    private long completedWorldTime = -1L;

    public MissionProgress(String missionId, long startedWorldTime) {
        this.missionId = Mission.normalizeId(missionId);
        this.startedWorldTime = Math.max(0L, startedWorldTime);
    }

    public String getMissionId() {
        return missionId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
    }

    public void addProgress(int amount) {
        if (amount > 0) {
            setProgress(progress + amount);
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getStartedWorldTime() {
        return startedWorldTime;
    }

    public long getCompletedWorldTime() {
        return completedWorldTime;
    }

    public void complete(long worldTime) {
        completed = true;
        completedWorldTime = Math.max(0L, worldTime);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(TAG_ID, missionId);
        compound.setInteger(TAG_PROGRESS, progress);
        compound.setBoolean(TAG_COMPLETED, completed);
        compound.setLong(TAG_STARTED_WORLD_TIME, startedWorldTime);
        compound.setLong(TAG_COMPLETED_WORLD_TIME, completedWorldTime);
        return compound;
    }

    public static MissionProgress readFromNBT(NBTTagCompound compound) {
        MissionProgress progress = new MissionProgress(compound.getString(TAG_ID), compound.getLong(TAG_STARTED_WORLD_TIME));
        progress.setProgress(compound.getInteger(TAG_PROGRESS));
        if (compound.getBoolean(TAG_COMPLETED)) {
            progress.complete(compound.hasKey(TAG_COMPLETED_WORLD_TIME) ? compound.getLong(TAG_COMPLETED_WORLD_TIME) : 0L);
        }
        return progress;
    }
}
