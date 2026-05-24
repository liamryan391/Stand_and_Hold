package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanStage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

public final class HumanWorldData extends WorldSavedData {
    public static final String DATA_NAME = StandAndHoldConstants.MOD_ID + "_human_progression";

    private static final String TAG_HUMAN_POINTS = "HumanPoints";
    private static final String TAG_STAGE = "Stage";

    private int humanPoints;
    private HumanStage stage = HumanStage.SURVIVORS;

    public HumanWorldData() {
        super(DATA_NAME);
    }

    public HumanWorldData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        humanPoints = Math.max(0, compound.getInteger(TAG_HUMAN_POINTS));
        stage = HumanStage.byId(compound.getInteger(TAG_STAGE));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger(TAG_HUMAN_POINTS, humanPoints);
        compound.setInteger(TAG_STAGE, stage.getId());
        return compound;
    }

    public int getHumanPoints() {
        return humanPoints;
    }

    public void setHumanPoints(int humanPoints) {
        int clampedPoints = Math.max(0, humanPoints);
        if (this.humanPoints != clampedPoints) {
            this.humanPoints = clampedPoints;
            markDirty();
        }
    }

    public void addHumanPoints(int amount) {
        if (amount != 0) {
            long newTotal = (long) humanPoints + amount;
            setHumanPoints(newTotal > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) newTotal);
        }
    }

    public HumanStage getStage() {
        return stage;
    }

    public void setStage(HumanStage stage) {
        HumanStage safeStage = stage == null ? HumanStage.SURVIVORS : stage;
        if (this.stage != safeStage) {
            this.stage = safeStage;
            markDirty();
        }
    }
}
