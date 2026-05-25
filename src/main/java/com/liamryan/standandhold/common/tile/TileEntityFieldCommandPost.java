package com.liamryan.standandhold.common.tile;

import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.infrastructure.MainBaseManager;
import com.liamryan.standandhold.common.infrastructure.OutpostDefenseManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public final class TileEntityFieldCommandPost extends TileEntity implements ITickable {
    private static final String TAG_PLACED_WORLD_TIME = "PlacedWorldTime";
    private static final String TAG_LAST_POINT_GENERATION_TIME = "LastPointGenerationTime";
    private static final String TAG_LAST_SUPPLY_GENERATION_TIME = "LastSupplyGenerationTime";
    private static final String TAG_LAST_DEFENDER_SPAWN_TIME = "LastDefenderSpawnTime";
    private static final String TAG_LAST_SPECIAL_DIVISION_DEPLOYMENT_TIME = "LastSpecialDivisionDeploymentTime";
    private static final String TAG_UPGRADE_LEVEL = "UpgradeLevel";
    private static final String TAG_STORED_SUPPLIES = "StoredSupplies";

    private long placedWorldTime = -1L;
    private long lastPointGenerationTime = -1L;
    private long lastSupplyGenerationTime = -1L;
    private long lastDefenderSpawnTime = -1L;
    private long lastSpecialDivisionDeploymentTime = -1L;
    private int upgradeLevel = FieldCommandPostLevel.FIELD_CAMP.getLevel();
    private int storedSupplies;

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
            if (lastSupplyGenerationTime < 0L) {
                lastSupplyGenerationTime = world.getTotalWorldTime();
                changed = true;
            }
            if (lastDefenderSpawnTime < 0L) {
                lastDefenderSpawnTime = world.getTotalWorldTime();
                changed = true;
            }
            if (lastSpecialDivisionDeploymentTime < 0L) {
                lastSpecialDivisionDeploymentTime = world.getTotalWorldTime();
                changed = true;
            }
            if (changed) {
                markDirty();
            }
        }
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        updatePointGeneration();
        updateSupplyGeneration();
        OutpostDefenseManager.updateOutpostDefense(this);
        MainBaseManager.updateMainBase(this);
    }

    private void updatePointGeneration() {
        if (!StandAndHoldConfig.infrastructure.enableFieldCommandPostPointGeneration) {
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

    private void updateSupplyGeneration() {
        if (!StandAndHoldConfig.supply.enableCommandPostSupplyGeneration) {
            return;
        }

        int suppliesPerInterval = StandAndHoldConfig.supply.commandPostSuppliesPerInterval;
        if (suppliesPerInterval <= 0) {
            return;
        }

        int tickInterval = Math.max(1, StandAndHoldConfig.supply.commandPostSupplyTickInterval);
        long worldTime = world.getTotalWorldTime();
        if (lastSupplyGenerationTime < 0L) {
            lastSupplyGenerationTime = worldTime;
            markDirty();
            return;
        }

        if (worldTime - lastSupplyGenerationTime >= tickInterval) {
            int acceptedSupplies = addStoredSupplies(suppliesPerInterval);
            if (acceptedSupplies > 0) {
                SupplyManager.addSupplies(world, acceptedSupplies, "field command post supply generation: " + pos);
            }
            lastSupplyGenerationTime = worldTime;
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        placedWorldTime = compound.hasKey(TAG_PLACED_WORLD_TIME) ? compound.getLong(TAG_PLACED_WORLD_TIME) : -1L;
        lastPointGenerationTime = compound.hasKey(TAG_LAST_POINT_GENERATION_TIME) ? compound.getLong(TAG_LAST_POINT_GENERATION_TIME) : -1L;
        lastSupplyGenerationTime = compound.hasKey(TAG_LAST_SUPPLY_GENERATION_TIME) ? compound.getLong(TAG_LAST_SUPPLY_GENERATION_TIME) : -1L;
        lastDefenderSpawnTime = compound.hasKey(TAG_LAST_DEFENDER_SPAWN_TIME) ? compound.getLong(TAG_LAST_DEFENDER_SPAWN_TIME) : -1L;
        lastSpecialDivisionDeploymentTime = compound.hasKey(TAG_LAST_SPECIAL_DIVISION_DEPLOYMENT_TIME) ? compound.getLong(TAG_LAST_SPECIAL_DIVISION_DEPLOYMENT_TIME) : -1L;
        upgradeLevel = compound.hasKey(TAG_UPGRADE_LEVEL) ? FieldCommandPostLevel.byLevel(compound.getInteger(TAG_UPGRADE_LEVEL)).getLevel() : FieldCommandPostLevel.FIELD_CAMP.getLevel();
        storedSupplies = Math.max(0, compound.getInteger(TAG_STORED_SUPPLIES));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong(TAG_PLACED_WORLD_TIME, placedWorldTime);
        compound.setLong(TAG_LAST_POINT_GENERATION_TIME, lastPointGenerationTime);
        compound.setLong(TAG_LAST_SUPPLY_GENERATION_TIME, lastSupplyGenerationTime);
        compound.setLong(TAG_LAST_DEFENDER_SPAWN_TIME, lastDefenderSpawnTime);
        compound.setLong(TAG_LAST_SPECIAL_DIVISION_DEPLOYMENT_TIME, lastSpecialDivisionDeploymentTime);
        compound.setInteger(TAG_UPGRADE_LEVEL, getUpgradeLevelInfo().getLevel());
        compound.setInteger(TAG_STORED_SUPPLIES, getStoredSupplies());
        return compound;
    }

    public long getPlacedWorldTime() {
        return placedWorldTime;
    }

    public long getLastPointGenerationTime() {
        return lastPointGenerationTime;
    }

    public long getLastSupplyGenerationTime() {
        return lastSupplyGenerationTime;
    }

    public long getLastDefenderSpawnTime() {
        return lastDefenderSpawnTime;
    }

    public long getLastSpecialDivisionDeploymentTime() {
        return lastSpecialDivisionDeploymentTime;
    }

    public void setLastDefenderSpawnTime(long lastDefenderSpawnTime) {
        if (this.lastDefenderSpawnTime != lastDefenderSpawnTime) {
            this.lastDefenderSpawnTime = lastDefenderSpawnTime;
            markDirty();
        }
    }

    public void setLastSpecialDivisionDeploymentTime(long lastSpecialDivisionDeploymentTime) {
        if (this.lastSpecialDivisionDeploymentTime != lastSpecialDivisionDeploymentTime) {
            this.lastSpecialDivisionDeploymentTime = lastSpecialDivisionDeploymentTime;
            markDirty();
        }
    }

    public int getUpgradeLevel() {
        return getUpgradeLevelInfo().getLevel();
    }

    public FieldCommandPostLevel getUpgradeLevelInfo() {
        return FieldCommandPostLevel.byLevel(upgradeLevel);
    }

    public void setUpgradeLevel(int upgradeLevel) {
        int safeLevel = FieldCommandPostLevel.byLevel(upgradeLevel).getLevel();
        if (this.upgradeLevel != safeLevel) {
            this.upgradeLevel = safeLevel;
            markDirty();
        }
    }

    public int addStoredSupplies(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int maxStoredSupplies = getMaxStoredSupplies();
        if (storedSupplies >= maxStoredSupplies) {
            return 0;
        }

        int accepted = Math.min(amount, maxStoredSupplies - storedSupplies);
        storedSupplies += accepted;
        markDirty();
        return accepted;
    }

    public int getStoredSupplies() {
        return Math.min(storedSupplies, getMaxStoredSupplies());
    }

    public int getMaxStoredSupplies() {
        return Math.max(0, StandAndHoldConfig.supply.commandPostMaxStoredSupplies);
    }
}
