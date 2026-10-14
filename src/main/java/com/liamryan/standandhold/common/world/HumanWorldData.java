package com.liamryan.standandhold.common.world;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Objects;
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
    public static final int CURRENT_VERSION = 1;
    public static final Codec<HumanWorldData> CODEC = CompoundTag.CODEC.xmap(HumanWorldData::load, HumanWorldData::save);
    public static final SavedDataType<HumanWorldData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "human_progression"), HumanWorldData::new, CODEC);
    private static final Logger LOGGER = LogUtils.getLogger();

    private int dataVersion = CURRENT_VERSION;
    private long points;
    private HumanStage stage = HumanStage.SURVIVORS;
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
        data.migrateIfNeeded();
        if (storedPoints != data.points || storedStage != data.stage.level()
                || !isIntegral(tag.get("points")) || !isIntegral(tag.get("stage"))
                || !isIntegral(tag.get("data_version"))) {
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
        }
        return tag;
    }

    private static boolean isIntegral(Tag value) {
        return value instanceof ByteTag || value instanceof ShortTag || value instanceof IntTag || value instanceof LongTag;
    }

    private static long integral(Tag value, long fallback) {
        return isIntegral(value) ? ((NumericTag) value).longValue() : fallback;
    }
}
