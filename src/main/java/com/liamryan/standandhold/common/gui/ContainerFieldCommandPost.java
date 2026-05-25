package com.liamryan.standandhold.common.gui;

import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

public final class ContainerFieldCommandPost extends Container {
    private static final int FIELD_HUMAN_POINTS = 0;
    private static final int FIELD_STAGE_ID = 1;
    private static final int FIELD_SUPPLY_POINTS = 2;
    private static final int FIELD_UPGRADE_LEVEL = 3;
    private static final int FIELD_STORED_SUPPLIES = 4;
    private static final int FIELD_MAX_STORED_SUPPLIES = 5;

    private final TileEntityFieldCommandPost commandPost;
    private int humanPoints = -1;
    private int stageId = -1;
    private int supplyPoints = -1;
    private int upgradeLevel = -1;
    private int storedSupplies = -1;
    private int maxStoredSupplies = -1;

    public ContainerFieldCommandPost(InventoryPlayer playerInventory, TileEntityFieldCommandPost commandPost) {
        this.commandPost = commandPost;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return commandPost != null
                && commandPost.getWorld() != null
                && commandPost.getWorld().getTileEntity(commandPost.getPos()) == commandPost
                && player.getDistanceSq(commandPost.getPos()) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (commandPost == null || commandPost.getWorld() == null) {
            return;
        }

        HumanWorldData data = HumanPointManager.getData(commandPost.getWorld());
        syncField(FIELD_HUMAN_POINTS, data.getHumanPoints());
        syncField(FIELD_STAGE_ID, data.getStage().getId());
        syncField(FIELD_SUPPLY_POINTS, data.getSupplyPoints());
        syncField(FIELD_UPGRADE_LEVEL, commandPost.getUpgradeLevel());
        syncField(FIELD_STORED_SUPPLIES, commandPost.getStoredSupplies());
        syncField(FIELD_MAX_STORED_SUPPLIES, commandPost.getMaxStoredSupplies());
    }

    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case FIELD_HUMAN_POINTS:
                humanPoints = data;
                break;
            case FIELD_STAGE_ID:
                stageId = data;
                break;
            case FIELD_SUPPLY_POINTS:
                supplyPoints = data;
                break;
            case FIELD_UPGRADE_LEVEL:
                upgradeLevel = data;
                break;
            case FIELD_STORED_SUPPLIES:
                storedSupplies = data;
                break;
            case FIELD_MAX_STORED_SUPPLIES:
                maxStoredSupplies = data;
                break;
            default:
                break;
        }
    }

    public int getHumanPoints() {
        return humanPoints;
    }

    public int getStageId() {
        return stageId;
    }

    public int getSupplyPoints() {
        return supplyPoints;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public int getStoredSupplies() {
        return storedSupplies;
    }

    public int getMaxStoredSupplies() {
        return maxStoredSupplies;
    }

    private void syncField(int id, int value) {
        if (getCachedValue(id) == value) {
            return;
        }

        setCachedValue(id, value);
        for (IContainerListener listener : listeners) {
            listener.sendWindowProperty(this, id, value);
        }
    }

    private int getCachedValue(int id) {
        switch (id) {
            case FIELD_HUMAN_POINTS:
                return humanPoints;
            case FIELD_STAGE_ID:
                return stageId;
            case FIELD_SUPPLY_POINTS:
                return supplyPoints;
            case FIELD_UPGRADE_LEVEL:
                return upgradeLevel;
            case FIELD_STORED_SUPPLIES:
                return storedSupplies;
            case FIELD_MAX_STORED_SUPPLIES:
                return maxStoredSupplies;
            default:
                return Integer.MIN_VALUE;
        }
    }

    private void setCachedValue(int id, int value) {
        updateProgressBar(id, value);
    }
}
