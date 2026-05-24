package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.research.ResearchEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class HumanWorldData extends WorldSavedData {
    public static final String DATA_NAME = StandAndHoldConstants.MOD_ID + "_human_progression";

    private static final String TAG_HUMAN_POINTS = "HumanPoints";
    private static final String TAG_STAGE = "Stage";
    private static final String TAG_COMPLETED_RESEARCH = "CompletedResearch";

    private int humanPoints;
    private HumanStage stage = HumanStage.SURVIVORS;
    private final Set<String> completedResearchIds = new LinkedHashSet<String>();

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
        completedResearchIds.clear();

        NBTTagList completedResearchTags = compound.getTagList(TAG_COMPLETED_RESEARCH, Constants.NBT.TAG_STRING);
        for (int i = 0; i < completedResearchTags.tagCount(); i++) {
            String researchId = ResearchEntry.normalizeId(completedResearchTags.getStringTagAt(i));
            if (!researchId.isEmpty()) {
                completedResearchIds.add(researchId);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger(TAG_HUMAN_POINTS, humanPoints);
        compound.setInteger(TAG_STAGE, stage.getId());

        NBTTagList completedResearchTags = new NBTTagList();
        for (String researchId : completedResearchIds) {
            completedResearchTags.appendTag(new NBTTagString(researchId));
        }
        compound.setTag(TAG_COMPLETED_RESEARCH, completedResearchTags);
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

    public boolean isResearchCompleted(String researchId) {
        return completedResearchIds.contains(ResearchEntry.normalizeId(researchId));
    }

    public boolean completeResearch(String researchId) {
        String normalizedResearchId = ResearchEntry.normalizeId(researchId);
        if (normalizedResearchId.isEmpty() || completedResearchIds.contains(normalizedResearchId)) {
            return false;
        }

        completedResearchIds.add(normalizedResearchId);
        markDirty();
        return true;
    }

    public Set<String> getCompletedResearchIds() {
        return Collections.unmodifiableSet(completedResearchIds);
    }
}
