package com.liamryan.standandhold;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.mission.MissionState;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;

final class FoundationChecks {
    private static int assertions;

    static void run() throws Exception {
        definitions();
        transactions();
        missions();
        System.out.println("Foundation checks passed: " + assertions + " assertions.");
    }

    private static void definitions() throws Exception {
        FoundationCatalog catalog = defaults();
        check(catalog.research().size() == 3 && catalog.missions().size() == 7, "bounded starter definitions");
        check(catalog.research("parasite_samples").supplyCost() == 10, "short command IDs");
        check(catalog.mission("recover_parasite_samples").target() == 3, "sample objective");
        expect(() -> catalog.research("missing"));
        expect(() -> catalog.mission("BAD ID"));
        expect(() -> FoundationCatalog.parse("{"));
        expect(() -> FoundationCatalog.parse("null"));
        expect(() -> FoundationCatalog.parse("{\"format\":2}"));
        ResearchEntry first = catalog.research("parasite_samples");
        expect(() -> new FoundationCatalog(List.of(first, first), List.of()));
        ResearchEntry missing = research("standandhold:a", List.of("standandhold:missing"));
        expect(() -> new FoundationCatalog(List.of(missing), List.of()));
        expect(() -> new FoundationCatalog(List.of(research("standandhold:a", List.of("standandhold:b")),
                research("standandhold:b", List.of("standandhold:a"))), List.of()));
        expect(() -> new FoundationCatalog(List.of(research("standandhold:a", List.of("standandhold:a"))), List.of()));
        expect(() -> research("Not Namespaced", List.of()));
        expect(() -> new FoundationReward(-1, 0));
        Mission a = mission("standandhold:a", List.of("standandhold:b"));
        Mission b = mission("standandhold:b", List.of("standandhold:a"));
        expect(() -> new FoundationCatalog(List.of(), List.of(a, b)));
        expect(() -> new FoundationCatalog(List.of(), List.of(a, a)));
        expect(() -> new FoundationCatalog(List.of(), List.of(a)));
        expect(() -> Mission.Objective.parse("not_an_objective"));
        for (String bad : List.of("1.5", "9223372036854775808", "-1", "\"10\"", "null")) {
            expect(() -> FoundationCatalog.parse("{\"format\":1,\"research\":[{\"id\":\"standandhold:x\","
                    + "\"title\":\"X\",\"supply_cost\":" + bad + "}]}"));
        }
        expect(() -> FoundationCatalog.read(new ByteArrayInputStream(new byte[65_537])));
    }

    static FoundationCatalog defaults() throws Exception {
        try (InputStream stream = FoundationChecks.class.getResourceAsStream("/data/standandhold/foundation/definitions.json")) {
            if (stream == null) { throw new AssertionError("Missing default definitions resource."); }
            return FoundationCatalog.read(stream);
        }
    }

    private static void transactions() throws Exception {
        FoundationCatalog catalog = defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        HumanWorldData data = new HumanWorldData();
        CompoundTag before = encode(data);
        expect(() -> ResearchManager.complete(data, catalog, thresholds, "parasite_samples"));
        check(encode(data).equals(before) && !data.isDirty(), "insufficient cost is atomic");
        expect(() -> SupplyManager.add(data, 0));
        expect(() -> SupplyManager.add(data, -1));
        expect(() -> SupplyManager.spend(data, 1));
        check(SupplyManager.add(data, 10) == 10 && data.isDirty(), "supply credit");
        data.setDirty(false);
        check(!data.setSupplies(10) && !data.isDirty(), "identical supply balance is not dirty");
        check(ResearchManager.complete(data, catalog, thresholds, "parasite_samples"), "first research completes");
        check(data.points() == 10 && data.supplies() == 0 && data.isResearchComplete("standandhold:parasite_samples"), "research costs and reward");
        check(ResearchManager.hasUnlock(data, catalog, "sample_analysis"), "research capability unlock");
        data.setDirty(false);
        check(!ResearchManager.complete(data, catalog, thresholds, "parasite_samples") && !data.isDirty(), "repeat completion no-op");
        check(data.points() == 10 && data.supplies() == 0, "repeat cannot charge or reward");
        expect(() -> ResearchManager.complete(data, catalog, thresholds, "field_communications"));
        HumanWorldData reloaded = decode(encode(data));
        check(reloaded.supplies() == 0 && reloaded.isResearchComplete("standandhold:parasite_samples") && !reloaded.isDirty(), "research reload");

        HumanWorldData overflow = new HumanWorldData();
        overflow.setProgression(Long.MAX_VALUE, HumanStage.MAIN_BASE_COUNTER_OFFENSIVE);
        overflow.setSupplies(10);
        before = encode(overflow);
        expect(() -> ResearchManager.complete(overflow, catalog, thresholds, "parasite_samples"));
        check(encode(overflow).equals(before), "point reward overflow cannot spend or complete");
        overflow.setSupplies(Long.MAX_VALUE);
        before = encode(overflow);
        expect(() -> SupplyManager.add(overflow, 1));
        check(encode(overflow).equals(before), "supply overflow atomic");
        ResearchEntry supplyReward = new ResearchEntry("standandhold:overflow", "Test", 0, List.of(), 0, 0,
                new FoundationReward(0, 1), List.of());
        FoundationCatalog custom = new FoundationCatalog(List.of(supplyReward), List.of());
        expect(() -> ResearchManager.complete(overflow, custom, thresholds, "overflow"));
        check(encode(overflow).equals(before), "supply reward overflow cannot complete research");

        HumanWorldData gated = new HumanWorldData();
        gated.setProgression(5200, HumanStage.SPECIAL_PARASITE_DIVISION);
        gated.setSupplies(200);
        expect(() -> ResearchManager.complete(gated, catalog, thresholds, "field_communications"));
        ResearchManager.complete(gated, catalog, thresholds, "parasite_samples");
        ResearchManager.complete(gated, catalog, thresholds, "field_communications");
        before = encode(gated);
        expect(() -> ResearchManager.complete(gated, catalog, thresholds, "special_division_training"));
        check(encode(gated).equals(before), "physical sample requirement fails without spending");
        check(SupplyManager.spend(gated, 10) == 160, "supply debit");

        CompoundTag v1 = new CompoundTag();
        v1.putInt("data_version", 1);
        v1.putLong("points", 500);
        v1.putInt("stage", 2);
        v1.putString("unknown", "retained");
        HumanWorldData migrated = decode(v1);
        check(migrated.dataVersion() == 2 && migrated.points() == 500 && migrated.supplies() == 0, "M2 migration retains progression");
        check(encode(migrated).getStringOr("unknown", "").equals("retained"), "unknown top-level field survives");
        CompoundTag receipt = new CompoundTag();
        receipt.putBoolean("completed", true);
        receipt.putString("pack_extra", "retained");
        CompoundTag research = new CompoundTag();
        research.put("pack:removed_research", receipt);
        CompoundTag saved = encode(data);
        saved.put("research", research);
        HumanWorldData unknown = decode(saved);
        check(unknown.isResearchComplete("pack:removed_research"), "completion without reward flag is conservatively claimed");
        check(encode(unknown).getCompoundOrEmpty("research").equals(research), "unknown research IDs/fields preserved");
        saved.putString("research", "broken");
        HumanWorldData malformed = decode(saved);
        expect(() -> ResearchManager.complete(malformed, catalog, thresholds, "parasite_samples"));
        check(encode(malformed).getStringOr("research", "").equals("broken"), "malformed map not silently overwritten");
    }

    private static void missions() throws Exception {
        FoundationCatalog catalog = defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        HumanWorldData data = new HumanWorldData();
        expect(() -> MissionManager.start(data, catalog, thresholds, "establish_field_command"));
        expect(() -> MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", 1));
        MissionManager.start(data, catalog, thresholds, "recover_parasite_samples");
        data.setDirty(false);
        check(MissionManager.refresh(data, catalog, thresholds).isEmpty() && !data.isDirty(), "no tick/no-op refresh not dirty");
        expect(() -> MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", 0));
        check(MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", 2).state().progress() == 2, "partial sample objective");
        check(data.points() == 0 && data.supplies() == 0, "partial progress no reward");
        HumanWorldData reloaded = compressedRoundTrip(data);
        check(reloaded.mission("standandhold:recover_parasite_samples").orElseThrow().progress() == 2 && !reloaded.isDirty(), "partial mission compressed reload");
        var completed = MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", Long.MAX_VALUE);
        check(completed.state().completed() && completed.state().progress() == 3, "large debug increment safely caps at target");
        check(data.points() == 25 && data.supplies() == 10, "mission reward applies together");
        data.setDirty(false);
        check(!MissionManager.advance(data, catalog, thresholds, "recover_parasite_samples", 1).changed(), "repeat completion no-op");
        check(data.points() == 25 && data.supplies() == 10 && !data.isDirty(), "mission receipt prevents duplicate reward");
        expect(() -> data.saveMission("standandhold:recover_parasite_samples", new MissionState(0, false), data.points(), data.stage(), data.supplies()));
        expect(() -> data.saveMission("standandhold:recover_parasite_samples", completed.state(), data.points() + 25, data.stage(), data.supplies()));
        MissionManager.start(data, catalog, thresholds, "establish_field_command");
        MissionManager.advance(data, catalog, thresholds, "establish_field_command", 1);
        check(MissionManager.start(data, catalog, thresholds, "stockpile_supplies").state().completed(), "eligible stockpile recognized on start");
        MissionManager.start(data, catalog, thresholds, "complete_first_research");
        ResearchManager.complete(data, catalog, thresholds, "parasite_samples");
        check(MissionManager.refresh(data, catalog, thresholds).size() == 1, "real research state refresh completes objective");
        check(data.points() == 110 && data.supplies() == 20, "research and mission rewards do not recurse");
        MissionManager.start(data, catalog, thresholds, "early_army_response");
        data.setProgression(150, HumanStage.LOCAL_ARMY_RESPONSE);
        check(MissionManager.refresh(data, catalog, thresholds).size() == 1 && data.points() == 175, "stage objective observes progression");
        MissionManager.start(data, catalog, thresholds, "defend_outpost");
        MissionManager.advance(data, catalog, thresholds, "defend_outpost", 5);
        MissionManager.start(data, catalog, thresholds, "establish_main_base");
        MissionManager.advance(data, catalog, thresholds, "establish_main_base", 1);
        check(data.points() == 250 && data.supplies() == 55, "small chain finishes with expected balances");
        reloaded = compressedRoundTrip(data);
        check(reloaded.points() == 250 && reloaded.supplies() == 55 && !reloaded.isDirty(), "supplies and full mission chain reload");
        for (Mission mission : catalog.missions().values()) {
            check(!MissionManager.advance(reloaded, catalog, thresholds, mission.id(), 1).changed(), "reloaded receipt " + mission.id());
        }
        check(reloaded.points() == 250 && reloaded.supplies() == 55 && !reloaded.isDirty(), "reloaded rewards apply once only");

        HumanWorldData overflow = new HumanWorldData();
        MissionManager.start(overflow, catalog, thresholds, "recover_parasite_samples");
        overflow.setProgression(Long.MAX_VALUE, HumanStage.MAIN_BASE_COUNTER_OFFENSIVE);
        overflow.setDirty(false);
        CompoundTag before = encode(overflow);
        expect(() -> MissionManager.advance(overflow, catalog, thresholds, "recover_parasite_samples", 3));
        check(encode(overflow).equals(before) && !overflow.isDirty(), "point overflow cannot complete mission or move progress");
        overflow.setProgression(0, HumanStage.SURVIVORS);
        overflow.setSupplies(Long.MAX_VALUE);
        before = encode(overflow);
        expect(() -> MissionManager.advance(overflow, catalog, thresholds, "recover_parasite_samples", 3));
        check(encode(overflow).equals(before), "supply overflow cannot award mission points");
        Mission immediate = new Mission("standandhold:immediate", "Test", Mission.Objective.SUPPLY_TOTAL, 1, "", List.of(), new FoundationReward(1, 0));
        FoundationCatalog immediateCatalog = new FoundationCatalog(List.of(), List.of(immediate));
        overflow.setProgression(Long.MAX_VALUE, HumanStage.MAIN_BASE_COUNTER_OFFENSIVE);
        overflow.setDirty(false);
        before = encode(overflow);
        expect(() -> MissionManager.start(overflow, immediateCatalog, thresholds, "immediate"));
        check(encode(overflow).equals(before) && !overflow.isDirty(), "immediate completion failure cannot partially activate a mission");

        CompoundTag saved = encode(data);
        CompoundTag unknown = new CompoundTag();
        unknown.putLong("progress", 7);
        unknown.putBoolean("rewarded", true);
        unknown.putString("extra", "retained");
        saved.getCompoundOrEmpty("missions").put("pack:removed_mission", unknown);
        HumanWorldData retained = decode(saved);
        check(retained.mission("pack:removed_mission").orElseThrow().completed(), "reward flag alone prevents replay");
        check(encode(retained).getCompoundOrEmpty("missions").getCompoundOrEmpty("pack:removed_mission").equals(unknown), "unknown mission IDs/fields retained");
        saved.getCompoundOrEmpty("missions").putString("standandhold:recover_parasite_samples", "broken");
        HumanWorldData malformedEntry = decode(saved);
        expect(() -> MissionManager.advance(malformedEntry, catalog, thresholds, "recover_parasite_samples", 1));
        check(MissionManager.refresh(malformedEntry, catalog, thresholds).size() == 1, "malformed mission reported instead of crashing refresh");
        check(encode(malformedEntry).getCompoundOrEmpty("missions").getStringOr("standandhold:recover_parasite_samples", "").equals("broken"), "malformed mission not replaced");
        saved.putString("missions", "broken map");
        HumanWorldData malformedMap = decode(saved);
        expect(() -> MissionManager.start(malformedMap, catalog, thresholds, "recover_parasite_samples"));
        check(encode(malformedMap).getStringOr("missions", "").equals("broken map"), "malformed map retained");
        saved = encode(data);
        saved.putInt("data_version", HumanWorldData.CURRENT_VERSION + 1);
        HumanWorldData future = decode(saved);
        expect(() -> SupplyManager.add(future, 1));
        expect(() -> ResearchManager.complete(future, catalog, thresholds, "parasite_samples"));
        expect(() -> MissionManager.advance(future, catalog, thresholds, "recover_parasite_samples", 1));
        check(encode(future).equals(saved), "future schema remains byte-for-byte equivalent NBT");
    }

    private static HumanWorldData compressedRoundTrip(HumanWorldData data) throws Exception {
        CompoundTag root = new CompoundTag();
        root.put("data", encode(data));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, bytes);
        CompoundTag restored = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()), NbtAccounter.unlimitedHeap());
        return decode(restored.getCompoundOrEmpty("data"));
    }

    static HumanWorldData decode(CompoundTag tag) {
        return HumanWorldData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    static CompoundTag encode(HumanWorldData data) {
        return (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
    }

    private static ResearchEntry research(String id, List<String> prerequisites) {
        return new ResearchEntry(id, "Test", 0, prerequisites, 0, 0, new FoundationReward(0, 0), List.of());
    }

    private static Mission mission(String id, List<String> prerequisites) {
        return new Mission(id, "Test", Mission.Objective.SAMPLE_RECOVERY, 1, "", prerequisites, new FoundationReward(0, 0));
    }

    static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }

    static void expect(Action action) {
        assertions++;
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        catch (Exception exception) { throw new AssertionError("Unexpected exception", exception); }
        throw new AssertionError("Expected rejected operation.");
    }

    @FunctionalInterface
    interface Action { void run() throws Exception; }
}
