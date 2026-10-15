package com.liamryan.standandhold;

import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;

/** Dependency-free rules/codec checks; command integration is tested in the NeoForge server. */
public final class ProgressionChecks {
    private static int assertions;

    public static void main(String[] args) throws Exception {
        thresholdsAndBounds();
        savedData();
        FoundationChecks.run();
        BuildingChecks.run();
        System.out.println("Progression checks passed: " + assertions + " assertions.");
    }

    private static void thresholdsAndBounds() {
        check(ProgressionRules.validThresholds(ProgressionRules.DEFAULT_THRESHOLDS), "default thresholds");
        check(ProgressionRules.validThresholds(List.of(0, 150, 500, 1200, 2600, 5200, 10000)), "TOML integer types");
        check(!ProgressionRules.validThresholds(List.of(0, 1)), "short list rejected");
        check(!ProgressionRules.validThresholds(List.of(1, 2, 3, 4, 5, 6, 7)), "nonzero stage zero rejected");
        check(!ProgressionRules.validThresholds(List.of(0, 1, 1, 3, 4, 5, 6)), "duplicates rejected");
        check(!ProgressionRules.validThresholds(List.of(0, -1, 2, 3, 4, 5, 6)), "negative rejected");
        check(!ProgressionRules.validThresholds(List.of(0, 1.5, 2, 3, 4, 5, 6)), "fractional rejected");
        check(!ProgressionRules.validThresholds("bad"), "non-list rejected");
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        for (HumanStage stage : HumanStage.values()) {
            int level = stage.level();
            check(ProgressionRules.stageFor(thresholds[level], thresholds) == stage, "exact boundary " + level);
            if (level > 0) {
                check(ProgressionRules.stageFor(thresholds[level] - 1, thresholds).level() == level - 1,
                        "below boundary " + level);
            }
        }
        check(ProgressionRules.stageFor(Long.MAX_VALUE, thresholds).level() == 6, "maximum point stage");
        check(ProgressionRules.addExact(Long.MAX_VALUE - 1, 1) == Long.MAX_VALUE, "last safe increment");
        expect(IllegalArgumentException.class, () -> ProgressionRules.addExact(Long.MAX_VALUE, 1));
        expect(IllegalArgumentException.class, () -> ProgressionRules.addExact(1, Long.MAX_VALUE));
        expect(IllegalArgumentException.class, () -> ProgressionRules.addExact(0, 0));
        expect(IllegalArgumentException.class, () -> ProgressionRules.addExact(0, -1));
        expect(IllegalArgumentException.class, () -> ProgressionRules.stageFor(-1, thresholds));
        expect(IllegalArgumentException.class, () -> HumanStage.fromLevel(-1));
        expect(IllegalArgumentException.class, () -> HumanStage.fromLevel(7));
    }

    private static void savedData() throws Exception {
        CompoundTag old = new CompoundTag();
        old.putLong("points", 321);
        old.putInt("stage", 1);
        old.putString("future_field", "keep me");
        HumanWorldData migrated = decode(old);
        check(migrated.dataVersion() == HumanWorldData.CURRENT_VERSION && migrated.points() == 321, "version zero migration keeps points");
        check(migrated.isDirty(), "migration dirty");
        check(encode(migrated).getStringOr("future_field", "").equals("keep me"), "unknown field retained");
        migrated.setDirty(false);
        check(!migrated.setProgression(321, HumanStage.LOCAL_ARMY_RESPONSE), "same state is no-op");
        check(!migrated.isDirty(), "no-op not dirty");
        check(migrated.setProgression(500, HumanStage.ORGANISED_MILITARY), "point change");
        check(migrated.isDirty(), "mutation dirty");
        expect(IllegalArgumentException.class, () -> migrated.setProgression(-1, HumanStage.SURVIVORS));
        check(migrated.points() == 500, "rejected update is atomic");

        CompoundTag root = new CompoundTag();
        root.put("data", encode(migrated));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, bytes);
        CompoundTag disk = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()), NbtAccounter.unlimitedHeap());
        HumanWorldData reloaded = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, disk.get("data")).getOrThrow();
        check(reloaded.points() == 500 && reloaded.stage() == HumanStage.ORGANISED_MILITARY, "compressed save/reload");
        check(!reloaded.isDirty(), "valid reload not dirty");
        reloaded.setProgression(500, HumanStage.ELITE_UNITS);
        check(reloaded.isDirty(), "stage-only repair dirty");

        HumanWorldData empty = decode(new CompoundTag());
        check(empty.points() == 0 && empty.stage() == HumanStage.SURVIVORS && empty.dataVersion() == HumanWorldData.CURRENT_VERSION,
                "missing fields default safely");
        CompoundTag malformed = new CompoundTag();
        malformed.putString("data_version", "bad");
        malformed.putString("points", "bad");
        malformed.putInt("stage", 99);
        HumanWorldData repaired = decode(malformed);
        check(repaired.points() == 0 && repaired.stage() == HumanStage.SURVIVORS && repaired.isDirty(), "malformed fields");
        malformed.putInt("data_version", 1);
        malformed.putLong("points", -8);
        check(decode(malformed).points() == 0, "negative saved points");

        CompoundTag future = encode(migrated);
        future.putInt("data_version", HumanWorldData.CURRENT_VERSION + 1);
        future.putString("newer_system", "must not lose this");
        HumanWorldData unsupported = decode(future);
        check(!unsupported.isDirty(), "future schema not dirty");
        expect(IllegalStateException.class, () -> unsupported.setProgression(0, HumanStage.SURVIVORS));
        unsupported.setDirty();
        check(encode(unsupported).equals(future), "future data preserved even on forced save");
        future.putLong("data_version", Long.MAX_VALUE);
        HumanWorldData hugeVersion = decode(future);
        expect(IllegalStateException.class, hugeVersion::requireSupportedVersion);
        check(encode(hugeVersion).equals(future), "large future version cannot wrap into migration");
    }

    private static HumanWorldData decode(CompoundTag tag) {
        return HumanWorldData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    private static CompoundTag encode(HumanWorldData data) {
        return (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }

    private static void expect(Class<? extends Throwable> type, ThrowingAction action) {
        assertions++;
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) { return; }
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + error, error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
