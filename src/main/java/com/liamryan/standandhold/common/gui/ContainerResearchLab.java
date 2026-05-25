package com.liamryan.standandhold.common.gui;

import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

public final class ContainerResearchLab extends Container {
    private static final int FIELD_PROGRESS = 0;
    private static final int FIELD_PROGRESS_REQUIRED = 1;
    private static final int FIELD_STORED_SAMPLES = 2;
    private static final int FIELD_MAX_STORED_SAMPLES = 3;
    private static final int FIELD_STORED_SUPPLIES = 4;
    private static final int FIELD_MAX_STORED_SUPPLIES = 5;

    private final TileEntityResearchLab researchLab;
    private int progress = -1;
    private int progressRequired = -1;
    private int storedSamples = -1;
    private int maxStoredSamples = -1;
    private int storedSupplies = -1;
    private int maxStoredSupplies = -1;

    public ContainerResearchLab(InventoryPlayer playerInventory, TileEntityResearchLab researchLab) {
        this.researchLab = researchLab;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return researchLab != null
                && researchLab.getWorld() != null
                && researchLab.getWorld().getTileEntity(researchLab.getPos()) == researchLab
                && player.getDistanceSq(researchLab.getPos()) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (researchLab == null) {
            return;
        }

        syncField(FIELD_PROGRESS, researchLab.getResearchProgress());
        syncField(FIELD_PROGRESS_REQUIRED, researchLab.getResearchProgressRequired());
        syncField(FIELD_STORED_SAMPLES, researchLab.getStoredParasiteSamples());
        syncField(FIELD_MAX_STORED_SAMPLES, researchLab.getMaxStoredParasiteSamples());
        syncField(FIELD_STORED_SUPPLIES, researchLab.getStoredSupplies());
        syncField(FIELD_MAX_STORED_SUPPLIES, researchLab.getMaxStoredSupplies());
    }

    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case FIELD_PROGRESS:
                progress = data;
                break;
            case FIELD_PROGRESS_REQUIRED:
                progressRequired = data;
                break;
            case FIELD_STORED_SAMPLES:
                storedSamples = data;
                break;
            case FIELD_MAX_STORED_SAMPLES:
                maxStoredSamples = data;
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

    public int getProgress() {
        return progress;
    }

    public int getProgressRequired() {
        return progressRequired;
    }

    public int getStoredSamples() {
        return storedSamples;
    }

    public int getMaxStoredSamples() {
        return maxStoredSamples;
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
            case FIELD_PROGRESS:
                return progress;
            case FIELD_PROGRESS_REQUIRED:
                return progressRequired;
            case FIELD_STORED_SAMPLES:
                return storedSamples;
            case FIELD_MAX_STORED_SAMPLES:
                return maxStoredSamples;
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
