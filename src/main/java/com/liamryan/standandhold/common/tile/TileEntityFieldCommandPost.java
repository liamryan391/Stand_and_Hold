package com.liamryan.standandhold.common.tile;

import com.liamryan.standandhold.common.progression.HumanPointManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityFieldCommandPost extends TileEntity {
    private static final String TAG_PLACED_WORLD_TIME = "PlacedWorldTime";

    private long placedWorldTime = -1L;

    @Override
    public void onLoad() {
        if (world != null && !world.isRemote) {
            HumanPointManager.getData(world).registerFieldCommandPost(world.provider.getDimension(), pos);
            if (placedWorldTime < 0L) {
                placedWorldTime = world.getTotalWorldTime();
                markDirty();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        placedWorldTime = compound.getLong(TAG_PLACED_WORLD_TIME);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong(TAG_PLACED_WORLD_TIME, placedWorldTime);
        return compound;
    }

    public long getPlacedWorldTime() {
        return placedWorldTime;
    }
}
