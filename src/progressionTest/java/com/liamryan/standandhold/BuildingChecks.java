package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingState;
import com.liamryan.standandhold.common.building.BuildingTransactions;
import com.liamryan.standandhold.common.building.MenuValues;
import com.liamryan.standandhold.common.mission.MissionManager;
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

final class BuildingChecks {
    private static int assertions;
    static void run() throws Exception {
        transfer(); researchAndUpgrades(); savesAndPositions(); menuValues();
        System.out.println("Building checks passed: " + assertions + " assertions.");
    }
    private static void transfer() {
        BuildingState start = BuildingState.EMPTY;
        var imported = BuildingTransactions.transfer(start, 20, 10, 20, true);
        check(imported.local().supplies() == 10 && imported.global() == 10, "import conserves supplies");
        var exported = BuildingTransactions.transfer(imported.local(), imported.global(), 10, 20, false);
        check(exported.local().equals(start) && exported.global() == 20, "export reverses import");
        expect(() -> BuildingTransactions.transfer(start, 9, 10, 20, true));
        expect(() -> BuildingTransactions.transfer(start, 20, 10, 9, true));
        expect(() -> BuildingTransactions.transfer(start, 20, 0, 20, true));
        expect(() -> BuildingTransactions.transfer(start, 20, -1, 20, true));
        expect(() -> BuildingTransactions.transfer(start, 20, 10, 20, false));
        expect(() -> BuildingTransactions.transfer(imported.local(), Long.MAX_VALUE, 10, 20, false));
        expect(() -> BuildingTransactions.transfer(start.supplies(Long.MAX_VALUE), 20, 10, Long.MAX_VALUE, true));
        check(start.equals(BuildingState.EMPTY), "all failed transfers leave immutable local state untouched");
        var full = BuildingTransactions.transfer(start, 20, 20, 20, true);
        expect(() -> BuildingTransactions.transfer(full.local(), full.global(), 1, 20, true));
        check(full.local().supplies() == 20 && full.global() == 0, "sequential competing import cannot spend twice");
        var lowered = BuildingTransactions.transfer(full.local(), 0, 10, 5, false);
        check(lowered.local().supplies() == 10 && lowered.global() == 10, "lowered capacity still permits withdrawal");
        expect(() -> start.insertSamples(0));
        expect(() -> start.insertSamples(4097));
        check(start.insertSamples(4096).samples() == 4096, "bounded M5 sample insertion");
    }
    private static void researchAndUpgrades() throws Exception {
        var catalog = FoundationChecks.defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        var data = new HumanWorldData();
        var first = catalog.research("parasite_samples");
        var ready = BuildingState.EMPTY.supplies(10).target(first.id()).work(12);
        expect(() -> BuildingTransactions.finishResearch(ready.work(11), data, first, thresholds, 12));
        check(!data.isResearchComplete(first.id()) && data.points() == 0, "early work cannot spend/reward");
        expect(() -> BuildingTransactions.finishResearch(ready.supplies(9), data, first, thresholds, 12));
        check(data.supplies() == 0 && data.points() == 0, "local insufficiency cannot spend global balances");
        var finished = BuildingTransactions.finishResearch(ready, data, first, thresholds, 12);
        check(finished.supplies() == 0 && data.points() == 10 && data.isResearchComplete(first.id()), "lab consumes local cost and grants global receipt once");
        expect(() -> BuildingTransactions.finishResearch(ready, data, first, thresholds, 12));
        check(data.points() == 10 && ready.supplies() == 10, "second lab cannot pay/reward already completed research");
        check(ready.target("standandhold:field_communications").work() == 0, "changing/clearing target resets work");
        var cost = new BuildingTransactions.Upgrade(150, 20, 0, first.id());
        expect(() -> BuildingTransactions.upgrade(ready, data, cost));
        data.setProgression(150, HumanStage.LOCAL_ARMY_RESPONSE);
        expect(() -> BuildingTransactions.upgrade(ready, data, cost));
        var upgraded = BuildingTransactions.upgrade(ready.supplies(20), data, cost);
        check(upgraded.level() == 2 && upgraded.supplies() == 0 && data.points() == 150, "upgrade spends only local supply cost, points are a gate");
        expect(() -> BuildingTransactions.upgrade(upgraded, data, cost));
        check(upgraded.level() == 2 && data.points() == 150, "repeated click rechecks live cost");
        expect(() -> BuildingTransactions.upgrade(ready.supplies(50), data, new BuildingTransactions.Upgrade(0, 1, 0, "standandhold:missing")));
        expect(() -> BuildingTransactions.upgrade(new BuildingState(5, 100, 0, "", 0), data, cost));
        var later = catalog.research("special_division_training");
        check(!BuildingTransactions.researchBlocked(ready.supplies(100), data, later, thresholds).equals("Ready"), "late research remains gated");
        var overflow = new HumanWorldData(); overflow.setProgression(Long.MAX_VALUE, HumanStage.MAIN_BASE_COUNTER_OFFENSIVE);
        expect(() -> BuildingTransactions.finishResearch(ready, overflow, first, thresholds, 12));
        check(!overflow.isResearchComplete(first.id()) && ready.supplies() == 10, "overflow before receipt/cost leaves both unchanged");
    }
    private static void savesAndPositions() throws Exception {
        var original = new BuildingState(3, 321, 7, "standandhold:parasite_samples", 8);
        CompoundTag encoded = (CompoundTag) BuildingState.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); NbtIo.writeCompressed(encoded, bytes);
        CompoundTag roundTrip = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()), NbtAccounter.unlimitedHeap());
        check(BuildingState.CODEC.parse(NbtOps.INSTANCE, roundTrip).getOrThrow().equals(original), "compressed building save retains all fields");
        encoded.putString("target", "INVALID ID"); encoded.putLong("supplies", -1); encoded.putInt("level", 99); encoded.putInt("samples", -10);
        var safe = BuildingState.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        check(safe.level() == 5 && safe.supplies() == 0 && safe.samples() == 0 && safe.target().isEmpty() && safe.work() == 0, "malformed values normalize safely");
        check(BuildingState.CODEC.parse(NbtOps.INSTANCE, new CompoundTag()).getOrThrow().equals(BuildingState.EMPTY), "missing building fields initialize safely");
        String key = "minecraft:overworld@1,64,-1";
        var position = BuildingPosition.parse(key).orElseThrow();
        check(position.key().equals(key), "dimension-aware position round trip");
        for (String bad : List.of("", "bad", "@1,2,3", "UPPER CASE@1,2,3", "minecraft:overworld@x,2,3", "minecraft:overworld@1,2", "minecraft:overworld@1,99999,3", "minecraft:overworld@40000000,2,3")) {
            check(BuildingPosition.parse(bad).isEmpty(), "malformed/out-of-bounds position skipped: " + bad);
        }
        var data = new HumanWorldData();
        check(data.registerCommandPost(position), "first registration changes data"); data.setDirty(false);
        check(!data.registerCommandPost(position) && !data.isDirty(), "repeat load/registration does not dirty");
        CompoundTag saved = (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var restored = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        check(restored.commandPostKeys().equals(List.of(key)), "global registry survives reload");
        restored.removeCommandPost(key); check(restored.commandPostKeys().isEmpty(), "removal unregisters"); restored.setDirty(false);
        restored.removeCommandPost(key); check(!restored.isDirty(), "repeated removal no-op");
        saved.putInt("data_version", 2); saved.remove("command_posts");
        restored = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        check(restored.dataVersion() == 3 && restored.commandPostKeys().isEmpty(), "schema two migration adds registry without loss");
        var catalog = FoundationChecks.defaults(); long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        MissionManager.start(data, catalog, thresholds, "recover_parasite_samples");
        MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", 3);
        var update = MissionManager.start(data, catalog, thresholds, "establish_field_command");
        check(update.state().completed(), "existing registered command post advances newly activated mission");
        long points = data.points(); MissionManager.refresh(data, catalog, thresholds);
        check(data.points() == points, "building mission receipt prevents repeated rewards");
    }
    private static void menuValues() {
        for (long value : new long[] {0, 32767, 32768, 65535, 65536, Integer.MAX_VALUE, 4294967296L, Long.MAX_VALUE, -1}) {
            long result = 0;
            for (int limb = 0; limb < 4; limb++) { result = MenuValues.accept(result, limb, (short) MenuValues.limb(value, limb)); }
            check(result == value, "signed-short network limbs round trip: " + value);
        }
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
    private static void expect(Runnable action) {
        assertions++;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("Expected safely rejected building operation");
    }
}
