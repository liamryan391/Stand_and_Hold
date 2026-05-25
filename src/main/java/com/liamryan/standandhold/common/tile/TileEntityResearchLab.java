package com.liamryan.standandhold.common.tile;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.supply.ISupplyStorage;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import java.util.ArrayList;
import java.util.List;

public final class TileEntityResearchLab extends TileEntity implements ITickable, ISupplyStorage {
    private static final String TAG_PLACED_WORLD_TIME = "PlacedWorldTime";
    private static final String TAG_LAST_PROGRESS_TIME = "LastProgressTime";
    private static final String TAG_TARGET_RESEARCH_ID = "TargetResearchId";
    private static final String TAG_RESEARCH_PROGRESS = "ResearchProgress";
    private static final String TAG_STORED_PARASITE_SAMPLES = "StoredParasiteSamples";
    private static final String TAG_STORED_SUPPLIES = "StoredSupplies";

    private long placedWorldTime = -1L;
    private long lastProgressTime = -1L;
    private String targetResearchId = "";
    private int researchProgress;
    private int storedParasiteSamples;
    private int storedSupplies;

    @Override
    public void onLoad() {
        if (world != null && !world.isRemote) {
            HumanPointManager.getData(world).registerResearchLab(world.provider.getDimension(), pos);
            boolean changed = false;
            if (placedWorldTime < 0L) {
                placedWorldTime = world.getTotalWorldTime();
                changed = true;
            }
            if (lastProgressTime < 0L) {
                lastProgressTime = world.getTotalWorldTime();
                changed = true;
            }
            if (changed) {
                markDirty();
            }
        }
    }

    @Override
    public void update() {
        if (world == null || world.isRemote || !StandAndHoldConfig.research.enableResearchLabProgress) {
            return;
        }

        int progressPerInterval = StandAndHoldConfig.research.researchLabProgressPerInterval;
        if (progressPerInterval <= 0) {
            return;
        }

        int tickInterval = Math.max(1, StandAndHoldConfig.research.researchLabTickInterval);
        long worldTime = world.getTotalWorldTime();
        if (lastProgressTime < 0L) {
            lastProgressTime = worldTime;
            markDirty();
            return;
        }

        if (worldTime - lastProgressTime < tickInterval) {
            return;
        }

        lastProgressTime = worldTime;
        ResearchEntry target = getOrSelectTargetResearch();
        if (target == null) {
            return;
        }

        int requiredProgress = getResearchProgressRequired();
        int newProgress = Math.min(requiredProgress, researchProgress + progressPerInterval);
        if (newProgress != researchProgress) {
            researchProgress = newProgress;
            markDirty();
        }

        tryCompleteCurrentResearch();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        placedWorldTime = compound.hasKey(TAG_PLACED_WORLD_TIME) ? compound.getLong(TAG_PLACED_WORLD_TIME) : -1L;
        lastProgressTime = compound.hasKey(TAG_LAST_PROGRESS_TIME) ? compound.getLong(TAG_LAST_PROGRESS_TIME) : -1L;
        targetResearchId = compound.hasKey(TAG_TARGET_RESEARCH_ID) ? ResearchEntry.normalizeId(compound.getString(TAG_TARGET_RESEARCH_ID)) : "";
        researchProgress = Math.max(0, compound.getInteger(TAG_RESEARCH_PROGRESS));
        storedParasiteSamples = Math.max(0, compound.getInteger(TAG_STORED_PARASITE_SAMPLES));
        storedSupplies = Math.max(0, compound.getInteger(TAG_STORED_SUPPLIES));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong(TAG_PLACED_WORLD_TIME, placedWorldTime);
        compound.setLong(TAG_LAST_PROGRESS_TIME, lastProgressTime);
        compound.setString(TAG_TARGET_RESEARCH_ID, targetResearchId);
        compound.setInteger(TAG_RESEARCH_PROGRESS, Math.min(researchProgress, getResearchProgressRequired()));
        compound.setInteger(TAG_STORED_PARASITE_SAMPLES, Math.min(storedParasiteSamples, getMaxStoredParasiteSamples()));
        compound.setInteger(TAG_STORED_SUPPLIES, getStoredSupplies());
        return compound;
    }

    public boolean addStoredParasiteSamples(int amount) {
        if (amount <= 0) {
            return true;
        }

        int maxStoredSamples = getMaxStoredParasiteSamples();
        if (storedParasiteSamples >= maxStoredSamples) {
            return false;
        }

        storedParasiteSamples = Math.min(maxStoredSamples, storedParasiteSamples + amount);
        markDirty();
        return true;
    }

    @Override
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

    @Override
    public int removeStoredSupplies(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int availableSupplies = getStoredSupplies();
        if (availableSupplies <= 0) {
            return 0;
        }

        int removed = Math.min(amount, availableSupplies);
        storedSupplies = availableSupplies - removed;
        markDirty();
        return removed;
    }

    public boolean tryCompleteCurrentResearch() {
        if (world == null || world.isRemote) {
            return false;
        }

        ResearchEntry target = getOrSelectTargetResearch();
        if (target == null || researchProgress < getResearchProgressRequired()) {
            return false;
        }

        if (storedParasiteSamples < target.getParasiteSampleCost()) {
            return false;
        }

        ResearchManager.CompletionResult result = ResearchManager.completeResearch(world, target.getId());
        if (result.getStatus() == ResearchManager.CompletionStatus.COMPLETED) {
            if (target.getParasiteSampleCost() > 0) {
                storedParasiteSamples -= target.getParasiteSampleCost();
            }
            StandAndHold.LOGGER.info("Research Lab at {} completed research '{}'.", pos, target.getId());
            targetResearchId = "";
            researchProgress = 0;
            markDirty();
            return true;
        }

        if (result.getStatus() == ResearchManager.CompletionStatus.ALREADY_COMPLETE) {
            targetResearchId = "";
            researchProgress = 0;
            markDirty();
            return false;
        }

        return false;
    }

    public boolean selectNextAvailableResearch() {
        if (world == null || world.isRemote) {
            return false;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        List<ResearchEntry> availableTargets = getAvailableResearchTargets(data);
        if (availableTargets.isEmpty()) {
            if (!targetResearchId.isEmpty() || researchProgress != 0) {
                targetResearchId = "";
                researchProgress = 0;
                markDirty();
            }
            return false;
        }

        int currentIndex = getTargetIndex(availableTargets, targetResearchId);
        ResearchEntry nextTarget = availableTargets.get((currentIndex + 1) % availableTargets.size());
        if (!targetResearchId.equals(nextTarget.getId())) {
            targetResearchId = nextTarget.getId();
            researchProgress = 0;
            markDirty();
        }
        return true;
    }

    public String getTargetResearchLabel() {
        ResearchEntry target = getOrSelectTargetResearch();
        if (target == null) {
            return "none";
        }

        return target.getId() + " (" + target.getDisplayName() + ")";
    }

    public int getResearchProgressRequired() {
        return Math.max(1, StandAndHoldConfig.research.researchLabProgressRequired);
    }

    public int getMaxStoredParasiteSamples() {
        return Math.max(0, StandAndHoldConfig.research.researchLabMaxStoredSamples);
    }

    public int getResearchProgress() {
        return Math.min(researchProgress, getResearchProgressRequired());
    }

    public int getStoredParasiteSamples() {
        return Math.min(storedParasiteSamples, getMaxStoredParasiteSamples());
    }

    public int getStoredSupplies() {
        return Math.min(storedSupplies, getMaxStoredSupplies());
    }

    @Override
    public int getMaxStoredSupplies() {
        return Math.max(0, StandAndHoldConfig.supply.researchLabMaxStoredSupplies);
    }

    public String getTargetResearchId() {
        return targetResearchId;
    }

    private ResearchEntry getOrSelectTargetResearch() {
        HumanWorldData data = HumanPointManager.getData(world);
        ResearchEntry currentTarget = ResearchManager.getResearchEntry(targetResearchId);
        if (isValidTarget(data, currentTarget)) {
            return currentTarget;
        }

        ResearchEntry nextTarget = getNextAvailableResearch(data);
        String nextTargetId = nextTarget == null ? "" : nextTarget.getId();
        if (!targetResearchId.equals(nextTargetId) || researchProgress != 0) {
            targetResearchId = nextTargetId;
            researchProgress = 0;
            markDirty();
        }
        return nextTarget;
    }

    private ResearchEntry getNextAvailableResearch(HumanWorldData data) {
        List<ResearchEntry> availableTargets = getAvailableResearchTargets(data);
        return availableTargets.isEmpty() ? null : availableTargets.get(0);
    }

    private List<ResearchEntry> getAvailableResearchTargets(HumanWorldData data) {
        List<ResearchEntry> availableTargets = new ArrayList<ResearchEntry>();
        for (ResearchEntry entry : ResearchManager.getResearchEntries()) {
            if (isValidTarget(data, entry)) {
                availableTargets.add(entry);
            }
        }
        return availableTargets;
    }

    private int getTargetIndex(List<ResearchEntry> availableTargets, String researchId) {
        String normalizedId = ResearchEntry.normalizeId(researchId);
        for (int i = 0; i < availableTargets.size(); i++) {
            if (availableTargets.get(i).getId().equals(normalizedId)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidTarget(HumanWorldData data, ResearchEntry entry) {
        if (entry == null || data.isResearchCompleted(entry.getId())) {
            return false;
        }

        List<String> missingRequirements = ResearchManager.getMissingRequirements(data, entry);
        return missingRequirements.isEmpty();
    }
}
