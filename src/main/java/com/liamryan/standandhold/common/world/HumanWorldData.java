package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.threat.ThreatRecord;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class HumanWorldData extends WorldSavedData {
    public static final String DATA_NAME = StandAndHoldConstants.MOD_ID + "_human_progression";

    private static final String TAG_HUMAN_POINTS = "HumanPoints";
    private static final String TAG_SUPPLY_POINTS = "SupplyPoints";
    private static final String TAG_STAGE = "Stage";
    private static final String TAG_COMPLETED_RESEARCH = "CompletedResearch";
    private static final String TAG_FIELD_COMMAND_POSTS = "FieldCommandPosts";
    private static final String TAG_RESEARCH_LABS = "ResearchLabs";
    private static final String TAG_MAIN_BASES = "MainBases";
    private static final String TAG_ACTIVE_MAIN_BASES = "ActiveMainBases";
    private static final String TAG_THREAT_RECORDS = "ThreatRecords";

    private int humanPoints;
    private int supplyPoints;
    private HumanStage stage = HumanStage.SURVIVORS;
    private final Set<String> completedResearchIds = new LinkedHashSet<String>();
    private final Set<String> fieldCommandPostPositions = new LinkedHashSet<String>();
    private final Set<String> researchLabPositions = new LinkedHashSet<String>();
    private final Set<String> mainBasePositions = new LinkedHashSet<String>();
    private final Set<String> activeMainBasePositions = new LinkedHashSet<String>();
    private final Map<String, ThreatRecord> threatRecords = new LinkedHashMap<String, ThreatRecord>();

    public HumanWorldData() {
        super(DATA_NAME);
    }

    public HumanWorldData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        humanPoints = Math.max(0, compound.getInteger(TAG_HUMAN_POINTS));
        supplyPoints = Math.max(0, compound.getInteger(TAG_SUPPLY_POINTS));
        stage = HumanStage.byId(compound.getInteger(TAG_STAGE));
        completedResearchIds.clear();
        fieldCommandPostPositions.clear();
        researchLabPositions.clear();
        mainBasePositions.clear();
        activeMainBasePositions.clear();
        threatRecords.clear();

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

        NBTTagList mainBaseTags = compound.getTagList(TAG_MAIN_BASES, Constants.NBT.TAG_STRING);
        for (int i = 0; i < mainBaseTags.tagCount(); i++) {
            String positionKey = mainBaseTags.getStringTagAt(i);
            if (isValidPositionKey(positionKey)) {
                mainBasePositions.add(positionKey);
            }
        }

        NBTTagList activeMainBaseTags = compound.getTagList(TAG_ACTIVE_MAIN_BASES, Constants.NBT.TAG_STRING);
        for (int i = 0; i < activeMainBaseTags.tagCount(); i++) {
            String positionKey = activeMainBaseTags.getStringTagAt(i);
            if (isValidPositionKey(positionKey) && mainBasePositions.contains(positionKey)) {
                activeMainBasePositions.add(positionKey);
            }
        }

        NBTTagList threatRecordTags = compound.getTagList(TAG_THREAT_RECORDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < threatRecordTags.tagCount(); i++) {
            ThreatRecord record = ThreatRecord.readFromNBT(threatRecordTags.getCompoundTagAt(i));
            threatRecords.put(record.getKey(), record);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger(TAG_HUMAN_POINTS, humanPoints);
        compound.setInteger(TAG_SUPPLY_POINTS, supplyPoints);
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

        NBTTagList mainBaseTags = new NBTTagList();
        for (String positionKey : mainBasePositions) {
            mainBaseTags.appendTag(new NBTTagString(positionKey));
        }
        compound.setTag(TAG_MAIN_BASES, mainBaseTags);

        NBTTagList activeMainBaseTags = new NBTTagList();
        for (String positionKey : activeMainBasePositions) {
            activeMainBaseTags.appendTag(new NBTTagString(positionKey));
        }
        compound.setTag(TAG_ACTIVE_MAIN_BASES, activeMainBaseTags);

        NBTTagList threatRecordTags = new NBTTagList();
        for (ThreatRecord record : threatRecords.values()) {
            threatRecordTags.appendTag(record.writeToNBT());
        }
        compound.setTag(TAG_THREAT_RECORDS, threatRecordTags);
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

    public int getSupplyPoints() {
        return supplyPoints;
    }

    public void setSupplyPoints(int supplyPoints) {
        int clampedSupplies = Math.max(0, supplyPoints);
        if (this.supplyPoints != clampedSupplies) {
            this.supplyPoints = clampedSupplies;
            markDirty();
        }
    }

    public void addSupplyPoints(int amount) {
        if (amount != 0) {
            long newTotal = (long) supplyPoints + amount;
            setSupplyPoints(newTotal > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) newTotal);
        }
    }

    public boolean consumeSupplyPoints(int amount) {
        if (amount <= 0) {
            return true;
        }

        if (supplyPoints < amount) {
            return false;
        }

        setSupplyPoints(supplyPoints - amount);
        return true;
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

    public boolean registerMainBase(int dimension, BlockPos commandPostPos, boolean active) {
        String positionKey = getPositionKey(dimension, commandPostPos);
        boolean changed = mainBasePositions.add(positionKey);
        if (active) {
            changed |= activeMainBasePositions.add(positionKey);
        }
        if (changed) {
            markDirty();
        }
        return changed;
    }

    public boolean isMainBaseRegistered(int dimension, BlockPos commandPostPos) {
        return mainBasePositions.contains(getPositionKey(dimension, commandPostPos));
    }

    public boolean isMainBaseActive(int dimension, BlockPos commandPostPos) {
        return activeMainBasePositions.contains(getPositionKey(dimension, commandPostPos));
    }

    public boolean setMainBaseActive(int dimension, BlockPos commandPostPos, boolean active) {
        String positionKey = getPositionKey(dimension, commandPostPos);
        if (!mainBasePositions.contains(positionKey)) {
            return false;
        }

        boolean changed = active ? activeMainBasePositions.add(positionKey) : activeMainBasePositions.remove(positionKey);
        if (changed) {
            markDirty();
        }
        return changed;
    }

    public Set<String> getMainBasePositions() {
        return Collections.unmodifiableSet(mainBasePositions);
    }

    public Set<String> getActiveMainBasePositions() {
        return Collections.unmodifiableSet(activeMainBasePositions);
    }

    public ThreatRecord getOrCreateThreatRecord(int dimension, int regionX, int regionZ) {
        String key = ThreatRecord.getKey(dimension, regionX, regionZ);
        ThreatRecord record = threatRecords.get(key);
        if (record == null) {
            record = new ThreatRecord(dimension, regionX, regionZ);
            threatRecords.put(key, record);
            markDirty();
        }
        return record;
    }

    public ThreatRecord getThreatRecord(int dimension, int regionX, int regionZ) {
        return threatRecords.get(ThreatRecord.getKey(dimension, regionX, regionZ));
    }

    public Collection<ThreatRecord> getThreatRecords() {
        return Collections.unmodifiableCollection(threatRecords.values());
    }

    public boolean removeThreatRecord(ThreatRecord record) {
        if (record == null) {
            return false;
        }

        if (threatRecords.remove(record.getKey()) != null) {
            markDirty();
            return true;
        }
        return false;
    }

    public void pruneThreatRecords(int maxRecords) {
        int safeMaxRecords = Math.max(1, maxRecords);
        if (threatRecords.size() <= safeMaxRecords) {
            return;
        }

        Iterator<String> iterator = threatRecords.keySet().iterator();
        while (threatRecords.size() > safeMaxRecords && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        markDirty();
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
