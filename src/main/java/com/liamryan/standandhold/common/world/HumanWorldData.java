package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.block.ModBlocks;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.mission.MissionProgress;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import com.liamryan.standandhold.common.threat.ThreatRecord;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public static final int CURRENT_DATA_VERSION = 1;

    private static final String TAG_DATA_VERSION = "DataVersion";
    private static final String TAG_HUMAN_POINTS = "HumanPoints";
    private static final String TAG_SUPPLY_POINTS = "SupplyPoints";
    private static final String TAG_STAGE = "Stage";
    private static final String TAG_COMPLETED_RESEARCH = "CompletedResearch";
    private static final String TAG_FIELD_COMMAND_POSTS = "FieldCommandPosts";
    private static final String TAG_RESEARCH_LABS = "ResearchLabs";
    private static final String TAG_MAIN_BASES = "MainBases";
    private static final String TAG_ACTIVE_MAIN_BASES = "ActiveMainBases";
    private static final String TAG_THREAT_RECORDS = "ThreatRecords";
    private static final String TAG_MISSIONS = "Missions";

    private int dataVersion = CURRENT_DATA_VERSION;
    private int humanPoints;
    private int supplyPoints;
    private HumanStage stage = HumanStage.SURVIVORS;
    private final Set<String> completedResearchIds = new LinkedHashSet<String>();
    private final Set<String> fieldCommandPostPositions = new LinkedHashSet<String>();
    private final Set<String> researchLabPositions = new LinkedHashSet<String>();
    private final Set<String> mainBasePositions = new LinkedHashSet<String>();
    private final Set<String> activeMainBasePositions = new LinkedHashSet<String>();
    private final Map<String, ThreatRecord> threatRecords = new LinkedHashMap<String, ThreatRecord>();
    private final Map<String, MissionProgress> missionProgressRecords = new LinkedHashMap<String, MissionProgress>();

    public HumanWorldData() {
        super(DATA_NAME);
    }

    public HumanWorldData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        int savedDataVersion = compound.hasKey(TAG_DATA_VERSION, Constants.NBT.TAG_INT)
                ? Math.max(0, compound.getInteger(TAG_DATA_VERSION))
                : 0;
        dataVersion = savedDataVersion;
        humanPoints = Math.max(0, compound.getInteger(TAG_HUMAN_POINTS));
        supplyPoints = Math.max(0, compound.getInteger(TAG_SUPPLY_POINTS));
        stage = HumanStage.byId(compound.getInteger(TAG_STAGE));
        completedResearchIds.clear();
        fieldCommandPostPositions.clear();
        researchLabPositions.clear();
        mainBasePositions.clear();
        activeMainBasePositions.clear();
        threatRecords.clear();
        missionProgressRecords.clear();

        NBTTagList completedResearchTags = compound.getTagList(TAG_COMPLETED_RESEARCH, Constants.NBT.TAG_STRING);
        for (int i = 0; i < completedResearchTags.tagCount(); i++) {
            String researchId = ResearchEntry.normalizeId(completedResearchTags.getStringTagAt(i));
            if (!researchId.isEmpty()) {
                completedResearchIds.add(researchId);
            }
        }

        NBTTagList commandPostTags = compound.getTagList(TAG_FIELD_COMMAND_POSTS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < commandPostTags.tagCount(); i++) {
            PositionRecord record = parsePositionKey(commandPostTags.getStringTagAt(i));
            if (record != null) {
                fieldCommandPostPositions.add(record.toKey());
            }
        }

        NBTTagList researchLabTags = compound.getTagList(TAG_RESEARCH_LABS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < researchLabTags.tagCount(); i++) {
            PositionRecord record = parsePositionKey(researchLabTags.getStringTagAt(i));
            if (record != null) {
                researchLabPositions.add(record.toKey());
            }
        }

        NBTTagList mainBaseTags = compound.getTagList(TAG_MAIN_BASES, Constants.NBT.TAG_STRING);
        for (int i = 0; i < mainBaseTags.tagCount(); i++) {
            PositionRecord record = parsePositionKey(mainBaseTags.getStringTagAt(i));
            if (record != null) {
                mainBasePositions.add(record.toKey());
            }
        }

        NBTTagList activeMainBaseTags = compound.getTagList(TAG_ACTIVE_MAIN_BASES, Constants.NBT.TAG_STRING);
        for (int i = 0; i < activeMainBaseTags.tagCount(); i++) {
            PositionRecord record = parsePositionKey(activeMainBaseTags.getStringTagAt(i));
            String positionKey = record == null ? "" : record.toKey();
            if (!positionKey.isEmpty() && mainBasePositions.contains(positionKey)) {
                activeMainBasePositions.add(positionKey);
            }
        }

        NBTTagList threatRecordTags = compound.getTagList(TAG_THREAT_RECORDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < threatRecordTags.tagCount(); i++) {
            ThreatRecord record = ThreatRecord.readFromNBT(threatRecordTags.getCompoundTagAt(i));
            threatRecords.put(record.getKey(), record);
        }

        NBTTagList missionTags = compound.getTagList(TAG_MISSIONS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < missionTags.tagCount(); i++) {
            MissionProgress progress = MissionProgress.readFromNBT(missionTags.getCompoundTagAt(i));
            if (!progress.getMissionId().isEmpty()) {
                missionProgressRecords.put(progress.getMissionId(), progress);
            }
        }

        if (migrateIfNeeded(savedDataVersion)) {
            markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger(TAG_DATA_VERSION, CURRENT_DATA_VERSION);
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

        NBTTagList missionTags = new NBTTagList();
        for (MissionProgress progress : missionProgressRecords.values()) {
            missionTags.appendTag(progress.writeToNBT());
        }
        compound.setTag(TAG_MISSIONS, missionTags);
        return compound;
    }

    public int getDataVersion() {
        return dataVersion;
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
        if (pos == null) {
            return false;
        }

        String positionKey = getPositionKey(dimension, pos);
        if (fieldCommandPostPositions.add(positionKey)) {
            markDirty();
            return true;
        }
        return false;
    }

    public boolean unregisterFieldCommandPost(int dimension, BlockPos pos) {
        if (pos == null) {
            return false;
        }

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
        if (pos == null) {
            return false;
        }

        String positionKey = getPositionKey(dimension, pos);
        if (researchLabPositions.add(positionKey)) {
            markDirty();
            return true;
        }
        return false;
    }

    public boolean unregisterResearchLab(int dimension, BlockPos pos) {
        if (pos == null) {
            return false;
        }

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
        if (commandPostPos == null) {
            return false;
        }

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
        if (commandPostPos == null) {
            return false;
        }

        return mainBasePositions.contains(getPositionKey(dimension, commandPostPos));
    }

    public boolean isMainBaseActive(int dimension, BlockPos commandPostPos) {
        if (commandPostPos == null) {
            return false;
        }

        return activeMainBasePositions.contains(getPositionKey(dimension, commandPostPos));
    }

    public boolean setMainBaseActive(int dimension, BlockPos commandPostPos, boolean active) {
        if (commandPostPos == null) {
            return false;
        }

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

    public boolean unregisterMainBase(int dimension, BlockPos commandPostPos) {
        if (commandPostPos == null) {
            return false;
        }

        String positionKey = getPositionKey(dimension, commandPostPos);
        boolean changed = mainBasePositions.remove(positionKey);
        changed |= activeMainBasePositions.remove(positionKey);
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

    public MissionProgress getMissionProgress(String missionId) {
        return missionProgressRecords.get(Mission.normalizeId(missionId));
    }

    public Collection<MissionProgress> getMissionProgressRecords() {
        return Collections.unmodifiableCollection(missionProgressRecords.values());
    }

    public boolean startMission(String missionId, long worldTime) {
        String normalizedMissionId = Mission.normalizeId(missionId);
        if (normalizedMissionId.isEmpty() || missionProgressRecords.containsKey(normalizedMissionId)) {
            return false;
        }

        missionProgressRecords.put(normalizedMissionId, new MissionProgress(normalizedMissionId, worldTime));
        markDirty();
        return true;
    }

    public boolean setMissionProgress(String missionId, int progress) {
        MissionProgress record = getMissionProgress(missionId);
        if (record == null || record.isCompleted()) {
            return false;
        }

        int safeProgress = Math.max(0, progress);
        if (record.getProgress() == safeProgress) {
            return false;
        }

        record.setProgress(safeProgress);
        markDirty();
        return true;
    }

    public boolean addMissionProgress(String missionId, int amount) {
        MissionProgress record = getMissionProgress(missionId);
        if (record == null || record.isCompleted() || amount <= 0) {
            return false;
        }

        record.addProgress(amount);
        markDirty();
        return true;
    }

    public boolean completeMission(String missionId, long worldTime) {
        MissionProgress record = getMissionProgress(missionId);
        if (record == null || record.isCompleted()) {
            return false;
        }

        record.complete(worldTime);
        markDirty();
        return true;
    }

    public CleanupResult cleanupLoadedFieldCommandPostPositions(World world) {
        CleanupResult result = new CleanupResult("Field Command Post");
        if (!canCleanup(world)) {
            return result;
        }

        int dimension = world.provider.getDimension();
        Iterator<String> iterator = fieldCommandPostPositions.iterator();
        while (iterator.hasNext()) {
            String positionKey = iterator.next();
            PositionRecord record = parsePositionKey(positionKey);
            if (record == null) {
                iterator.remove();
                result.incrementRemoved();
                continue;
            }

            if (record.dimension != dimension || !world.isBlockLoaded(record.pos)) {
                continue;
            }

            if (!isFieldCommandPostAt(world, record.pos)) {
                iterator.remove();
                result.incrementRemoved();
            }
        }

        markDirtyIfChanged(result);
        logCleanupResult(result);
        return result;
    }

    public CleanupResult cleanupLoadedResearchLabPositions(World world) {
        CleanupResult result = new CleanupResult("Research Lab");
        if (!canCleanup(world)) {
            return result;
        }

        int dimension = world.provider.getDimension();
        Iterator<String> iterator = researchLabPositions.iterator();
        while (iterator.hasNext()) {
            String positionKey = iterator.next();
            PositionRecord record = parsePositionKey(positionKey);
            if (record == null) {
                iterator.remove();
                result.incrementRemoved();
                continue;
            }

            if (record.dimension != dimension || !world.isBlockLoaded(record.pos)) {
                continue;
            }

            if (!isResearchLabAt(world, record.pos)) {
                iterator.remove();
                result.incrementRemoved();
            }
        }

        markDirtyIfChanged(result);
        logCleanupResult(result);
        return result;
    }

    public CleanupResult cleanupLoadedMainBasePositions(World world) {
        CleanupResult result = new CleanupResult("Main Base");
        if (!canCleanup(world)) {
            return result;
        }

        int dimension = world.provider.getDimension();
        Iterator<String> mainBaseIterator = mainBasePositions.iterator();
        while (mainBaseIterator.hasNext()) {
            String positionKey = mainBaseIterator.next();
            PositionRecord record = parsePositionKey(positionKey);
            if (record == null) {
                mainBaseIterator.remove();
                if (activeMainBasePositions.remove(positionKey)) {
                    result.incrementDeactivated();
                }
                result.incrementRemoved();
                continue;
            }

            if (record.dimension != dimension || !world.isBlockLoaded(record.pos)) {
                continue;
            }

            if (!isFieldCommandPostAt(world, record.pos)) {
                mainBaseIterator.remove();
                if (activeMainBasePositions.remove(positionKey)) {
                    result.incrementDeactivated();
                }
                result.incrementRemoved();
            }
        }

        Iterator<String> activeMainBaseIterator = activeMainBasePositions.iterator();
        while (activeMainBaseIterator.hasNext()) {
            String positionKey = activeMainBaseIterator.next();
            PositionRecord record = parsePositionKey(positionKey);
            if (record == null || !mainBasePositions.contains(positionKey)) {
                activeMainBaseIterator.remove();
                result.incrementDeactivated();
                continue;
            }

            if (record.dimension != dimension || !world.isBlockLoaded(record.pos)) {
                continue;
            }

            if (!isFieldCommandPostAt(world, record.pos)) {
                activeMainBaseIterator.remove();
                result.incrementDeactivated();
            }
        }

        markDirtyIfChanged(result);
        logCleanupResult(result);
        return result;
    }

    private boolean migrateIfNeeded(int oldVersion) {
        if (oldVersion >= CURRENT_DATA_VERSION) {
            dataVersion = CURRENT_DATA_VERSION;
            return false;
        }

        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Migrating Stand and Hold world data from version {} to {}.", oldVersion, CURRENT_DATA_VERSION);
        }

        boolean changed = false;
        changed |= normalizePositionSet(fieldCommandPostPositions);
        changed |= normalizePositionSet(researchLabPositions);
        changed |= normalizePositionSet(mainBasePositions);
        changed |= normalizePositionSet(activeMainBasePositions);
        changed |= activeMainBasePositions.retainAll(mainBasePositions);
        dataVersion = CURRENT_DATA_VERSION;
        if (StandAndHoldConfig.debugLogging && changed) {
            StandAndHold.LOGGER.info("Stand and Hold world data migration normalized saved position records.");
        }
        return true;
    }

    private boolean normalizePositionSet(Set<String> positionKeys) {
        Set<String> normalizedKeys = new LinkedHashSet<String>();
        boolean changed = false;
        for (String positionKey : positionKeys) {
            PositionRecord record = parsePositionKey(positionKey);
            if (record == null) {
                changed = true;
                continue;
            }

            String normalizedKey = record.toKey();
            normalizedKeys.add(normalizedKey);
            if (!normalizedKey.equals(positionKey)) {
                changed = true;
            }
        }

        if (changed || normalizedKeys.size() != positionKeys.size()) {
            positionKeys.clear();
            positionKeys.addAll(normalizedKeys);
            return true;
        }
        return false;
    }

    private boolean canCleanup(World world) {
        return world != null && !world.isRemote;
    }

    private boolean isFieldCommandPostAt(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != ModBlocks.FIELD_COMMAND_POST) {
            return false;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntityFieldCommandPost;
    }

    private boolean isResearchLabAt(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != ModBlocks.RESEARCH_LAB) {
            return false;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntityResearchLab;
    }

    private void markDirtyIfChanged(CleanupResult result) {
        if (result != null && result.hasChanges()) {
            markDirty();
        }
    }

    private void logCleanupResult(CleanupResult result) {
        if (result == null || !result.hasChanges() || !StandAndHoldConfig.debugLogging) {
            return;
        }

        StandAndHold.LOGGER.info(
                "Cleaned {} stale {} saved position records and deactivated {} active records.",
                result.getRemovedRecords(),
                result.getLabel(),
                result.getDeactivatedRecords()
        );
    }

    private static String getPositionKey(int dimension, BlockPos pos) {
        return dimension + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
    }

    private static PositionRecord parsePositionKey(String positionKey) {
        if (positionKey == null || positionKey.trim().isEmpty()) {
            return null;
        }

        String[] parts = positionKey.split(":");
        if (parts.length != 4) {
            return null;
        }

        try {
            int dimension = Integer.parseInt(parts[0]);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new PositionRecord(dimension, new BlockPos(x, y, z));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static final class CleanupResult {
        private final String label;
        private int removedRecords;
        private int deactivatedRecords;

        private CleanupResult(String label) {
            this.label = label;
        }

        private void incrementRemoved() {
            removedRecords++;
        }

        private void incrementDeactivated() {
            deactivatedRecords++;
        }

        public String getLabel() {
            return label;
        }

        public int getRemovedRecords() {
            return removedRecords;
        }

        public int getDeactivatedRecords() {
            return deactivatedRecords;
        }

        public boolean hasChanges() {
            return removedRecords > 0 || deactivatedRecords > 0;
        }
    }

    private static final class PositionRecord {
        private final int dimension;
        private final BlockPos pos;

        private PositionRecord(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        private String toKey() {
            return getPositionKey(dimension, pos);
        }
    }
}
