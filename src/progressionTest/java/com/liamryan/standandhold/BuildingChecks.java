package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingState;
import com.liamryan.standandhold.common.building.BuildingTransactions;
import com.liamryan.standandhold.common.building.MenuValues;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import com.liamryan.standandhold.common.research.ResearchEntry;
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
        transfer(); researchAndUpgrades(); savesAndPositions(); menuValues(); mainBaseNotices();
        check(MenuValues.statusCode("Requires standandhold:parasite_samples") == 3, "sample-named prerequisite is not a physical sample shortage");
        check(MenuValues.statusCode("Requires standandhold:stage_training") == 3, "stage-named prerequisite is not a stage shortage");
        check(MenuValues.statusCode("Requires 5 local samples") == 5, "physical sample shortage keeps its code");
        check(MenuValues.statusCode("Requires 20 local supplies") == 4, "physical supply shortage keeps its code");
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
    private static void mainBaseNotices() {
        for (int seen = 0; seen <= 3; seen++) {
            for (boolean active : new boolean[]{false, true}) {
                int next = BuildingTransactions.claimMainBaseNotice(seen, active);
                check(next == (seen | (active ? 2 : 1)), "claim only the observed base state, preserving prior notice");
                check(BuildingTransactions.claimMainBaseNotice(next, active) == next, "reopen/reload cannot claim a seen state again");
            }
        }
        expect(() -> BuildingTransactions.claimMainBaseNotice(-1, true));
        expect(() -> BuildingTransactions.claimMainBaseNotice(4, false));
    }
    private static void researchAndUpgrades() throws Exception {
        var catalog = FoundationChecks.defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        var data = new HumanWorldData();
        var first = catalog.research("parasite_samples");
        var selected = BuildingState.EMPTY.supplies(10).target(first.id()).work(12);
        check(!selected.researching(), "selecting research does not start it");
        expect(() -> BuildingTransactions.finishResearch(selected, data, first, thresholds, 12));
        var ready = BuildingTransactions.toggleResearch(selected, data, first, thresholds);
        check(ready.researching() && ready.work() == 12 && ready.supplies() == 10, "start preserves work and does not charge costs");
        var paused = BuildingTransactions.toggleResearch(ready, data, first, thresholds);
        check(!paused.researching() && paused.work() == 12 && paused.supplies() == 10, "pause preserves work and storage");
        check(BuildingTransactions.toggleResearch(paused, data, first, thresholds).equals(ready), "resume preserves progress");
        check(!ready.target(first.id()).researching(), "next selection resets to stopped");
        check(ready.supplies(20).insertSamples(1).researching(), "supply and sample transfers preserve running state");
        expect(() -> BuildingTransactions.finishResearch(ready.work(11), data, first, thresholds, 12));
        check(!data.isResearchComplete(first.id()) && data.points() == 0, "early work cannot spend/reward");
        expect(() -> BuildingTransactions.finishResearch(ready.supplies(9), data, first, thresholds, 12));
        check(data.supplies() == 0 && data.points() == 0, "local insufficiency cannot spend global balances");
        var finished = BuildingTransactions.finishResearch(ready, data, first, thresholds, 12);
        check(finished.supplies() == 0 && data.points() == 10 && data.isResearchComplete(first.id()), "lab consumes local cost and grants global receipt once");
        check(!finished.researching(), "completion stops the lab");
        expect(() -> BuildingTransactions.toggleResearch(finished, data, first, thresholds));
        expect(() -> BuildingTransactions.finishResearch(ready, data, first, thresholds, 12));
        check(data.points() == 10 && ready.supplies() == 10, "second lab cannot pay/reward already completed research");
        check(BuildingTransactions.researchBlocked(ready, data, first, thresholds).equals("Completed"), "completed research cannot re-enter the lab completion/notification branch");
        CompoundTag savedResearch = (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var reloadedResearch = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, savedResearch).getOrThrow();
        check(BuildingTransactions.researchBlocked(ready, reloadedResearch, first, thresholds).equals("Completed"), "reload preserves the completion/notification gate");
        expect(() -> BuildingTransactions.finishResearch(ready, reloadedResearch, first, thresholds, 12));
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
        var sampled = new ResearchEntry("standandhold:sample_api_test", "Sample API Test", 0, List.of(), 10, 2,
                new FoundationReward(1, 0), List.of());
        var sampledState = BuildingState.EMPTY.supplies(10).target(sampled.id()).work(2).running(true);
        var sampledData = new HumanWorldData();
        expect(() -> BuildingTransactions.finishResearch(sampledState, sampledData, sampled, thresholds, 2));
        check(sampledData.points() == 0 && !sampledData.isResearchComplete(sampled.id()), "missing samples cannot partially charge local/global state");
        var sampledFinish = BuildingTransactions.finishResearch(sampledState.insertSamples(2), sampledData, sampled, thresholds, 2);
        check(sampledFinish.samples() == 0 && sampledFinish.supplies() == 0 && sampledData.points() == 1, "bounded sample path consumes exact local costs once");
        var sampleUpgrade = new BuildingTransactions.Upgrade(0, 10, 2, "");
        expect(() -> BuildingTransactions.upgrade(ready, data, sampleUpgrade));
        check(BuildingTransactions.upgrade(ready.insertSamples(2), data, sampleUpgrade).samples() == 0, "upgrade consumes required sample count");
    }
    private static void savesAndPositions() throws Exception {
        var original = new BuildingState(3, 321, 7, "standandhold:parasite_samples", 8, true);
        CompoundTag encoded = (CompoundTag) BuildingState.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); NbtIo.writeCompressed(encoded, bytes);
        CompoundTag roundTrip = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()), NbtAccounter.unlimitedHeap());
        check(BuildingState.CODEC.parse(NbtOps.INSTANCE, roundTrip).getOrThrow().equals(original), "compressed building save retains all fields");
        encoded.putString("target", "INVALID ID"); encoded.putLong("supplies", -1); encoded.putInt("level", 99); encoded.putInt("samples", -10);
        var safe = BuildingState.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        check(safe.level() == 5 && safe.supplies() == 0 && safe.samples() == 0 && safe.target().isEmpty() && safe.work() == 0, "malformed values normalize safely");
        check(BuildingState.CODEC.parse(NbtOps.INSTANCE, new CompoundTag()).getOrThrow().equals(BuildingState.EMPTY), "missing building fields initialize safely");
        var oldLab = original.save(); oldLab.remove("researching");
        check(!BuildingState.CODEC.parse(NbtOps.INSTANCE, oldLab).getOrThrow().researching(), "old modern labs retain work but wait for explicit Start");
        String key = "minecraft:overworld@1,64,-1";
        var position = BuildingPosition.parse(key).orElseThrow();
        check(position.key().equals(key), "dimension-aware position round trip");
        for (String bad : List.of("", "bad", "@1,2,3", "UPPER CASE@1,2,3", "minecraft:overworld@x,2,3", "minecraft:overworld@1,2", "minecraft:overworld@1,99999,3", "minecraft:overworld@40000000,2,3")) {
            check(BuildingPosition.parse(bad).isEmpty(), "malformed/out-of-bounds position skipped: " + bad);
        }
        var data = new HumanWorldData();
        check(!data.hasCommandPosts(), "empty registry is not a mission objective");
        check(data.registerCommandPost(position), "first registration changes data"); data.setDirty(false);
        check(data.hasCommandPosts() && !data.isDirty(), "bounded unsorted mission observation is read-only");
        check(!data.registerCommandPost(position) && !data.isDirty(), "repeat load/registration does not dirty");
        CompoundTag saved = (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var restored = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        check(restored.commandPostKeys().equals(List.of(key)), "global registry survives reload");
        var nether = BuildingPosition.parse("minecraft:the_nether@1,64,-1").orElseThrow();
        restored.registerCommandPost(nether);
        check(restored.commandPostKeys().size() == 2, "same coordinates in different dimensions do not collide");
        restored.removeCommandPost(nether.key());
        restored.removeCommandPost(key); check(restored.commandPostKeys().isEmpty(), "removal unregisters"); restored.setDirty(false);
        restored.removeCommandPost(key); check(!restored.isDirty(), "repeated removal no-op");
        saved.putInt("data_version", 2); saved.remove("command_posts");
        restored = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        check(restored.dataVersion() == HumanWorldData.CURRENT_VERSION && restored.commandPostKeys().isEmpty(), "schema two migration adds registry without loss");
        var catalog = FoundationChecks.defaults(); long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        MissionManager.start(data, catalog, thresholds, "recover_parasite_samples");
        MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", 3);
        var update = MissionManager.start(data, catalog, thresholds, "establish_field_command");
        check(update.state().completed(), "existing registered command post advances newly activated mission");
        long points = data.points(); MissionManager.refresh(data, catalog, thresholds);
        check(data.points() == points, "building mission receipt prevents repeated rewards");
    }
    private static void menuValues() {
        check(MenuValues.progressWidth(0, 12, 282) == 0, "empty progress bar");
        check(MenuValues.progressWidth(6, 12, 282) == 141, "half progress bar");
        check(MenuValues.progressWidth(12, 12, 282) == 282, "completed progress bar");
        check(MenuValues.progressWidth(Long.MAX_VALUE, 12, 282) == 282, "progress bar cannot overflow its panel");
        check(MenuValues.progressWidth(-1, 12, 282) == 0 && MenuValues.progressWidth(1, 0, 282) == 0, "invalid progress remains bounded");
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
