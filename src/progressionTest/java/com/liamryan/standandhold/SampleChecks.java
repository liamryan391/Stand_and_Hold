package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingState;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.sample.SampleTransferRules;
import com.liamryan.standandhold.common.threat.ThreatRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

final class SampleChecks {
    private static int assertions;
    static void run() throws Exception {
        mappings(); transfers(); missions();
        System.out.println("Sample/threat checks passed: " + assertions + " assertions.");
    }
    private static void mappings() {
        var defaults = ThreatRules.parse(ThreatRules.DEFAULTS);
        check(defaults.issues().isEmpty() && defaults.rules().size() == 1, "safe zombie default");
        var zombie = defaults.match("minecraft:zombie", tag -> false).orElseThrow();
        check(zombie.points() == 5 && zombie.chance() == .35 && zombie.samples() == 1, "default testing reward");
        check(defaults.match("minecraft:pig", tag -> false).isEmpty(), "unconfigured type ignored");
        check(defaults.match(null, tag -> true).isEmpty() && defaults.match("", tag -> true).isEmpty(), "null/missing registry keys ignored even with tags");
        var selectors = ThreatRules.parse(List.of("#standandhold:test_threats|1|1|64", "minecraft:zombie|2|0|0", "#other:tag|99|1|1"));
        check(selectors.match("minecraft:zombie", tag -> true).orElseThrow().points() == 2, "exact IDs override tags without stacking");
        check(selectors.match("minecraft:husk", tag -> true).orElseThrow().points() == 1, "first matching tag wins");
        check(selectors.match("minecraft:pig", tag -> false).isEmpty(), "absent tags cannot match unrelated entities");
        check(ThreatRules.parse(List.of("missing_mod:missing|1|1|1")).match("minecraft:pig", tag -> false).isEmpty(), "absent mod ID never defaults to pig");
        var duplicate = ThreatRules.parse(List.of("minecraft:zombie|1|0|0", "minecraft:zombie|5|1|1"));
        check(duplicate.rules().size() == 1 && duplicate.rules().getFirst().points() == 1 && duplicate.issues().size() == 1, "duplicate selector first wins");
        List<Object> invalid = new ArrayList<>(List.of("", "zombie|1|1|1", "#|1|1|1", "Bad:id|1|1|1", "minecraft:zombie|1", "minecraft:zombie|-1|1|1",
                "minecraft:zombie|1.5|1|1", "minecraft:zombie|1000001|1|1", "minecraft:zombie|9223372036854775808|1|1", "minecraft:zombie|1|NaN|1",
                "minecraft:zombie|1|Infinity|1", "minecraft:zombie|1|-0.1|1", "minecraft:zombie|1|1.1|1", "minecraft:zombie|1|1|-1", "minecraft:zombie|1|1|65", 123));
        invalid.add(null);
        var rejected = ThreatRules.parse(invalid);
        check(rejected.rules().isEmpty() && rejected.issues().size() == invalid.size(), "malformed entries skipped independently without fallback rewards");
        check(ThreatRules.parse(List.of()).rules().isEmpty(), "empty list disables all mappings");
        List<String> huge = new ArrayList<>();
        for (int i = 0; i < 300; i++) { huge.add("test:entity_" + i + "|0|0|0"); }
        check(ThreatRules.parse(huge).rules().size() == 256, "hard mapping limit");
        for (double roll : new double[] {0, .1, .999999}) {
            check(new ThreatRules.Rule("test:x", false, 0, 0, 64).dropCount(roll) == 0, "zero chance never drops");
            check(new ThreatRules.Rule("test:x", false, 0, 1, 64).dropCount(roll) == 64, "one chance drops at most one bounded stack");
        }
        check(zombie.dropCount(.349999) == 1 && zombie.dropCount(.35) == 0, "strict chance boundary");
        check(zombie.dropCount(-1) == 0 && zombie.dropCount(Double.NaN) == 0, "invalid rolls never mint samples");
    }
    private static void transfers() {
        check(SampleTransferRules.deposit(3, 0) == 3 && SampleTransferRules.deposit(100, 0) == 64, "deposit available count capped at one stack");
        check(SampleTransferRules.deposit(64, 4095) == 1, "near-full storage accepts only exact remaining room");
        expect(() -> SampleTransferRules.deposit(1, 4096)); expect(() -> SampleTransferRules.deposit(0, 0));
        expect(() -> SampleTransferRules.deposit(-1, 0)); expect(() -> SampleTransferRules.deposit(1, 4097));
        check(SampleTransferRules.withdraw(10, 20) == 10 && SampleTransferRules.withdraw(100, 100) == 64, "withdrawal honors real inventory space/batch");
        expect(() -> SampleTransferRules.withdraw(0, 10)); expect(() -> SampleTransferRules.withdraw(10, 0));
        int held = 150, stored = 0;
        for (int i = 0; i < 3; i++) { int move = SampleTransferRules.deposit(held, stored); held -= move; stored += move; }
        check(held == 0 && stored == 150, "serialized deposits conserve items");
        expect(() -> SampleTransferRules.deposit(0, 150));
        int move = SampleTransferRules.withdraw(1, stored); held += move; stored -= move;
        check(held + stored == 150 && stored == 149, "partial inventory capacity cannot discard remainder");
        BuildingState state = BuildingState.EMPTY.insertSamples(stored);
        var encoded = BuildingState.CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();
        check(BuildingState.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow().samples() == 149, "sample bank persists in existing building schema");
    }
    private static void missions() throws Exception {
        var catalog = FoundationChecks.defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        var data = new HumanWorldData();
        check(MissionManager.observeSamples(data, catalog, thresholds, 3).isEmpty() && data.points() == 0, "no auto-start or reward before mission activation");
        MissionManager.start(data, catalog, thresholds, "recover_parasite_samples");
        check(MissionManager.observeSamples(data, catalog, thresholds, 1).size() == 1, "actual carried sample advances active objective");
        data.setDirty(false);
        check(MissionManager.observeSamples(data, catalog, thresholds, 1).isEmpty() && !data.isDirty(), "drop/re-pick same sample cannot advance/dirty/spam");
        check(MissionManager.observeSamples(data, catalog, thresholds, 0).isEmpty(), "depositing cannot decrease saved progress");
        MissionManager.observeSamples(data, catalog, thresholds, 2);
        CompoundTag saved = (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var restored = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        check(restored.mission("standandhold:recover_parasite_samples").orElseThrow().progress() == 2, "partial pickup objective persists");
        check(MissionManager.observeSamples(restored, catalog, thresholds, 3).size() == 1 && restored.points() == 25 && restored.supplies() == 10, "third carried sample rewards once");
        restored.setDirty(false);
        check(MissionManager.observeSamples(restored, catalog, thresholds, 64).isEmpty() && !restored.isDirty(), "repeated pickup/withdrawal cannot repeat reward");
        var overflow = new HumanWorldData();
        MissionManager.start(overflow, catalog, thresholds, "recover_parasite_samples");
        overflow.setProgression(Long.MAX_VALUE, HumanStage.MAIN_BASE_COUNTER_OFFENSIVE);
        var blocked = MissionManager.observeSamples(overflow, catalog, thresholds, 3);
        check(!blocked.isEmpty() && !overflow.mission("standandhold:recover_parasite_samples").orElseThrow().completed()
                && overflow.supplies() == 0, "overflow cannot partially complete a sample mission");
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
    private static void expect(Runnable action) {
        assertions++;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("Expected rejected sample operation");
    }
}
