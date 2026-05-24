package com.liamryan.standandhold.common.tile;

import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public final class TileEntityFieldCommandPost extends TileEntity implements ITickable {
    private static final String TAG_PLACED_WORLD_TIME = "PlacedWorldTime";
    private static final String TAG_LAST_POINT_GENERATION_TIME = "LastPointGenerationTime";

    private long placedWorldTime = -1L;
    private long lastPointGenerationTime = -1L;

    @Override
    public void onLoad() {
        if (world != null && !world.isRemote) {
            HumanPointManager.getData(world).registerFieldCommandPost(world.provider.getDimension(), pos);
            boolean changed = false;
            if (placedWorldTime < 0L) {
                placedWorldTime = world.getTotalWorldTime();
                changed = true;
            }
            if (lastPointGenerationTime < 0L) {
                lastPointGenerationTime = world.getTotalWorldTime();
                changed = true;
            }
            if (changed) {
                markDirty();
            }
        }
    }

    @Override
    public void update() {
        if (world == null || world.isRemote || !StandAndHoldConfig.infrastructure.enableFieldCommandPostPointGeneration) {
            return;
        }

        int pointsPerInterval = StandAndHoldConfig.infrastructure.fieldCommandPostPointsPerInterval;
        if (pointsPerInterval <= 0) {
            return;
        }

        int tickInterval = Math.max(1, StandAndHoldConfig.infrastructure.fieldCommandPostTickInterval);
        long worldTime = world.getTotalWorldTime();
        if (lastPointGenerationTime < 0L) {
            lastPointGenerationTime = worldTime;
            markDirty();
            return;
        }

        if (worldTime - lastPointGenerationTime >= tickInterval) {
            HumanPointManager.addPoints(world, pointsPerInterval, "field command post passive generation: " + pos);
            lastPointGenerationTime = worldTime;
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        placedWorldTime = compound.hasKey(TAG_PLACED_WORLD_TIME) ? compound.getLong(TAG_PLACED_WORLD_TIME) : -1L;
        lastPointGenerationTime = compound.hasKey(TAG_LAST_POINT_GENERATION_TIME) ? compound.getLong(TAG_LAST_POINT_GENERATION_TIME) : -1L;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong(TAG_PLACED_WORLD_TIME, placedWorldTime);
        compound.setLong(TAG_LAST_POINT_GENERATION_TIME, lastPointGenerationTime);
        return compound;
    }

    public long getPlacedWorldTime() {
        return placedWorldTime;
    }

    public long getLastPointGenerationTime() {
        return lastPointGenerationTime;
    }
}
