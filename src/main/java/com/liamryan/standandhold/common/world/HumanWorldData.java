package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.mission.MissionState;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

public final class HumanWorldData extends SavedData {
    public static final int CURRENT_VERSION = 2;
    public static final Codec<HumanWorldData> CODEC = CompoundTag.CODEC.xmap(HumanWorldData::load, HumanWorldData::save);
    public static final SavedDataType<HumanWorldData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "human_progression"), HumanWorldData::new, CODEC);
    private static final Logger LOGGER = LogUtils.getLogger();

    private int dataVersion = CURRENT_VERSION;
    private long points;
    private HumanStage stage = HumanStage.SURVIVORS;
    private long supplies;
    private CompoundTag research = new CompoundTag();
    private boolean malformedResearch;
    private CompoundTag missions = new CompoundTag();
    private boolean malformedMissions;
    private CompoundTag retainedFields = new CompoundTag();

    public static HumanWorldData get(MinecraftServer server) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Human progression must be accessed on the server thread.");
        }
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public int dataVersion() { return dataVersion; }
    public long points() { return points; }
    public HumanStage stage() { return stage; }
    public long supplies() { requireSupportedVersion(); return supplies; }

    public boolean setSupplies(long newSupplies) {
        requireSupportedVersion();
        if (newSupplies < 0) { throw new IllegalArgumentException("Supplies cannot be negative."); }
        if (supplies == newSupplies) { return false; }
        supplies = newSupplies;
        setDirty();
        return true;
    }

    public boolean isResearchComplete(String id) {
        requireSupportedVersion();
        FoundationCatalog.requireId(id);
        if (malformedResearch) { throw new IllegalStateException("Malformed saved research map; restore a backup before changing research."); }
        Tag raw = research.get(id);
        if (raw == null) { return false; }
        if (!(raw instanceof CompoundTag entry) || (!entry.contains("completed") && !entry.contains("rewarded"))) {
            throw new IllegalStateException("Malformed saved research entry: " + id);
        }
        return savedFlag(entry, "completed") | savedFlag(entry, "rewarded");
    }

    public boolean completeResearch(String id, long newPoints, HumanStage newStage, long newSupplies) {
        if (isResearchComplete(id)) { return false; }
        validateBalances(newPoints, newStage, newSupplies);
        CompoundTag entry = research.getCompoundOrEmpty(id).copy();
        entry.putBoolean("completed", true);
        entry.putBoolean("rewarded", true);
        // Balances and the reward receipt share one SavedData commit, with no callback between them.
        points = newPoints;
        stage = newStage;
        supplies = newSupplies;
        research.put(id, entry);
        setDirty();
        return true;
    }

    public Optional<MissionState> mission(String id) {
        requireSupportedVersion();
        FoundationCatalog.requireId(id);
        if (malformedMissions) { throw new IllegalStateException("Malformed saved mission map; restore a backup before changing missions."); }
        Tag raw = missions.get(id);
        if (raw == null) { return Optional.empty(); }
        if (!(raw instanceof CompoundTag entry)) { throw new IllegalStateException("Malformed saved mission entry: " + id); }
        long progress = integral(entry.get("progress"), 0);
        if (entry.contains("progress") && (!isIntegral(entry.get("progress")) || progress < 0)) {
            throw new IllegalStateException("Malformed saved mission progress: " + id);
        }
        boolean completed = savedFlag(entry, "completed");
        boolean rewarded = savedFlag(entry, "rewarded");
        // Either historical flag is proof of completion: never regrant an uncertain old reward.
        return Optional.of(new MissionState(progress, completed || rewarded));
    }

    public boolean saveMission(String id, MissionState state, long newPoints, HumanStage newStage, long newSupplies) {
        Optional<MissionState> old = mission(id);
        Objects.requireNonNull(state, "mission state");
        validateBalances(newPoints, newStage, newSupplies);
        boolean unchanged = old.filter(state::equals).isPresent()
                && points == newPoints && stage == newStage && supplies == newSupplies;
        if (old.filter(MissionState::completed).isPresent()) {
            if (!unchanged) { throw new IllegalStateException("Completed missions cannot be reset or rewarded again."); }
            return false;
        }
        if (unchanged) { return false; }
        CompoundTag entry = missions.getCompoundOrEmpty(id).copy();
        entry.putLong("progress", state.progress());
        entry.putBoolean("completed", state.completed());
        entry.putBoolean("rewarded", state.completed());
        points = newPoints;
        stage = newStage;
        supplies = newSupplies;
        missions.put(id, entry);
        setDirty();
        return true;
    }

    public void requireSupportedVersion() {
        if (dataVersion > CURRENT_VERSION) {
            throw new IllegalStateException("Human progression uses a newer save schema; restore a compatible mod before changing it.");
        }
    }

    public boolean setProgression(long newPoints, HumanStage newStage) {
        requireSupportedVersion();
        Objects.requireNonNull(newStage, "stage");
        if (newPoints < 0) {
            throw new IllegalArgumentException("Human points cannot be negative.");
        }
        if (points == newPoints && stage == newStage) {
            return false;
        }
        points = newPoints;
        stage = newStage;
        setDirty();
        return true;
    }

    private static HumanWorldData load(CompoundTag tag) {
        HumanWorldData data = new HumanWorldData();
        data.retainedFields = tag.copy();
        long storedVersion = integral(tag.get("data_version"), 0);
        data.dataVersion = (int) Math.clamp(storedVersion, 0, Integer.MAX_VALUE);
        long storedPoints = integral(tag.get("points"), 0);
        long storedStage = integral(tag.get("stage"), 0);
        data.points = Math.max(0, storedPoints);
        data.stage = storedStage >= 0 && storedStage < HumanStage.values().length
                ? HumanStage.fromLevel((int) storedStage) : HumanStage.SURVIVORS;
        // Future data remains untouched, even if another caller later marks this object dirty.
        if (data.dataVersion > CURRENT_VERSION) {
            return data;
        }
        long storedSupplies = integral(tag.get("supplies"), 0);
        data.supplies = Math.max(0, storedSupplies);
        data.research = tag.getCompoundOrEmpty("research").copy();
        data.malformedResearch = tag.contains("research") && !(tag.get("research") instanceof CompoundTag)
                || data.dataVersion >= 2 && !tag.contains("research");
        data.missions = tag.getCompoundOrEmpty("missions").copy();
        data.malformedMissions = tag.contains("missions") && !(tag.get("missions") instanceof CompoundTag)
                || data.dataVersion >= 2 && !tag.contains("missions");
        data.migrateIfNeeded();
        if (storedPoints != data.points || storedStage != data.stage.level()
                || !isIntegral(tag.get("points")) || !isIntegral(tag.get("stage"))
                || !isIntegral(tag.get("data_version")) || storedSupplies != data.supplies
                || !isIntegral(tag.get("supplies"))) {
            data.setDirty();
        }
        return data;
    }

    private void migrateIfNeeded() {
        if (dataVersion < CURRENT_VERSION) {
            LOGGER.debug("Migrating modern human progression schema {} to {} without discarding unknown fields.",
                    dataVersion, CURRENT_VERSION);
            dataVersion = CURRENT_VERSION;
            setDirty();
        }
    }

    private CompoundTag save() {
        CompoundTag tag = retainedFields.copy();
        if (dataVersion <= CURRENT_VERSION) {
            tag.putInt("data_version", CURRENT_VERSION);
            tag.putLong("points", points);
            tag.putInt("stage", stage.level());
            tag.putLong("supplies", supplies);
            if (!malformedResearch) { tag.put("research", research.copy()); }
            if (!malformedMissions) { tag.put("missions", missions.copy()); }
        }
        return tag;
    }

    private static boolean isIntegral(Tag value) {
        return value instanceof ByteTag || value instanceof ShortTag || value instanceof IntTag || value instanceof LongTag;
    }

    private static long integral(Tag value, long fallback) {
        return isIntegral(value) ? ((NumericTag) value).longValue() : fallback;
    }

    private static void validateBalances(long points, HumanStage stage, long supplies) {
        Objects.requireNonNull(stage, "stage");
        if (points < 0 || supplies < 0) { throw new IllegalArgumentException("Balances cannot be negative."); }
    }

    private static boolean savedFlag(CompoundTag entry, String key) {
        Tag raw = entry.get(key);
        if (raw == null) { return false; }
        if (!(raw instanceof ByteTag flag) || flag.byteValue() < 0 || flag.byteValue() > 1) {
            throw new IllegalStateException("Malformed saved completion flag: " + key);
        }
        return flag.byteValue() == 1;
    }
}
