package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.research.ResearchEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
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
    private static final String TAG_FIELD_COMMAND_POSTS = "FieldCommandPosts";
    private static final String TAG_RESEARCH_LABS = "ResearchLabs";

    private int humanPoints;
    private HumanStage stage = HumanStage.SURVIVORS;
    private final Set<String> completedResearchIds = new LinkedHashSet<String>();
    private final Set<String> fieldCommandPostPositions = new LinkedHashSet<String>();
    private final Set<String> researchLabPositions = new LinkedHashSet<String>();

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
        fieldCommandPostPositions.clear();
        researchLabPositions.clear();

        NBTTagList completedResearchTags = compound.getTagList(TAG_COMPLETED_RESEARCH, Constants.NBT.TAG_STRING);
        for (int i = 0; i < completedResearchTags.tagCount(); i++) {
            String researchId = ResearchEntry.normalizeId(completedResearchTags.getStringTagAt(i));
            if (!researchId.isEmpty()) {
                completedResearchIds.add(researchId);
            }
        }

        NBTTagList commandPostTags = compound.getTagList(TAG_FIELD_COMMAND_POSTS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < commandPostTags.tagCount(); i++) {
            String positionKey = commandPostTags.getStringTagAt(i);
            if (isValidPositionKey(positionKey)) {
                fieldCommandPostPositions.add(positionKey);
            }
        }

        NBTTagList researchLabTags = compound.getTagList(TAG_RESEARCH_LABS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < researchLabTags.tagCount(); i++) {
            String positionKey = researchLabTags.getStringTagAt(i);
            if (isValidPositionKey(positionKey)) {
                researchLabPositions.add(positionKey);
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

        NBTTagList commandPostTags = new NBTTagList();
        for (String positionKey : fieldCommandPostPositions) {
            commandPostTags.appendTag(new NBTTagString(positionKey));
        }
        compound.setTag(TAG_FIELD_COMMAND_POSTS, commandPostTags);

        NBTTagList researchLabTags = new NBTTagList();
        for (String positionKey : researchLabPositions) {
            researchLabTags.appendTag(new NBTTagString(positionKey));
        }
        compound.setTag(TAG_RESEARCH_LABS, researchLabTags);
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

    public boolean registerFieldCommandPost(int dimension, BlockPos pos) {
        String positionKey = getPositionKey(dimension, pos);
        if (fieldCommandPostPositions.add(positionKey)) {
            markDirty();
            return true;
        }
        return false;
    }

    public boolean unregisterFieldCommandPost(int dimension, BlockPos pos) {
        String positionKey = getPositionKey(dimension, pos);
        if (fieldCommandPostPositions.remove(positionKey)) {
            markDirty();
            return true;
        }
        return false;
    }

    public Set<String> getFieldCommandPostPositions() {
        return Collections.unmodifiableSet(fieldCommandPostPositions);
    }

    public boolean registerResearchLab(int dimension, BlockPos pos) {
        String positionKey = getPositionKey(dimension, pos);
        if (researchLabPositions.add(positionKey)) {
            markDirty();
            return true;
        }
        return false;
    }

    public boolean unregisterResearchLab(int dimension, BlockPos pos) {
        String positionKey = getPositionKey(dimension, pos);
        if (researchLabPositions.remove(positionKey)) {
            markDirty();
            return true;
        }
        return false;
    }

    public Set<String> getResearchLabPositions() {
        return Collections.unmodifiableSet(researchLabPositions);
    }

    private static String getPositionKey(int dimension, BlockPos pos) {
        return dimension + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
    }

    private static boolean isValidPositionKey(String positionKey) {
        if (positionKey == null || positionKey.trim().isEmpty()) {
            return false;
        }

        String[] parts = positionKey.split(":");
        if (parts.length != 4) {
            return false;
        }

        try {
            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[1]);
            Integer.parseInt(parts[2]);
            Integer.parseInt(parts[3]);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
