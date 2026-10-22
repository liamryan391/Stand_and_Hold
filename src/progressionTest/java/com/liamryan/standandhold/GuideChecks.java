package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingState;
import com.liamryan.standandhold.common.building.BuildingTransactions;
import com.liamryan.standandhold.common.command.CommandGuide;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.threat.ThreatRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;

final class GuideChecks {
    private static int assertions;
    static void run() throws Exception {
        var catalog = FoundationChecks.defaults();
        long[] thresholds = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
        var data = new HumanWorldData();
        for (Mission mission : catalog.missions().values()) {
            String text = readOnly(data, catalog, thresholds, true);
            check(text.contains(mission.title()) && text.contains("Start: /standandhold mission start " + mission.id()), "guide follows eligible catalog order without auto-start");
            MissionManager.start(data, catalog, thresholds, mission.id());
            if (!data.mission(mission.id()).orElseThrow().completed()) {
                text = readOnly(data, catalog, thresholds, false);
                check(!text.contains("Start:"), "active mission not started again");
                MissionManager.advance(data, catalog, thresholds, mission.id(), mission.target());
            }
        }
        check(readOnly(data, catalog, thresholds, true).contains("All missions in the loaded catalog are complete"), "completed catalog remains complete without repeated rewards");
        var partial = new HumanWorldData();
        MissionManager.start(partial, catalog, thresholds, "recover_parasite_samples");
        MissionManager.observeSamples(partial, catalog, thresholds, 2);
        check(readOnly(partial, catalog, thresholds, false).contains("2/3"), "actual stored partial progress shown without refreshing it");
        check(readOnly(new HumanWorldData(), catalog, thresholds, false).contains("Ask an operator"), "non-operator gets accurate start restriction");
        check(!String.join("\n", CommandGuide.help(false)).contains("QA/operator:"), "restricted command help separated by access");
        check(String.join("\n", CommandGuide.help(true)).contains("mission start <id>"), "operator QA entry points documented");
        var suggested = CommandGuide.component("Start: /standandhold mission start standandhold:recover_parasite_samples").getStyle().getClickEvent();
        check(suggested instanceof ClickEvent.SuggestCommand click && click.command().equals("/standandhold mission start standandhold:recover_parasite_samples"), "guide only suggests a command, never executes it");
        check(CommandGuide.component("Ask an operator to start shared mission test:mission.").getStyle().getClickEvent() == null, "non-operator guide cannot suggest a restricted mission start");
        var research = new ResearchEntry("test:analysis", "Custom Analysis", 2, List.of(), 17, 4, new FoundationReward(0, 0), List.of());
        var mission = new Mission("test:research", "Custom Research Goal", Mission.Objective.RESEARCH_COMPLETION, 1, research.id(), List.of(), new FoundationReward(0, 0));
        String custom = readOnly(new HumanWorldData(), new FoundationCatalog(List.of(research), List.of(mission)), thresholds, true);
        check(custom.contains("Custom Analysis") && custom.contains("Stage 2, 17 LOCAL supplies, 4 samples"), "requirements come from loaded definitions, not hardcoded defaults");
        var futureTag = new CompoundTag(); futureTag.putInt("data_version", HumanWorldData.CURRENT_VERSION + 1);
        var future = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, futureTag).getOrThrow();
        try { CommandGuide.lines(future, catalog, thresholds, true); throw new AssertionError("Future data must refuse guide access"); }
        catch (IllegalStateException expected) { assertions++; }
        defaultLoop(catalog, thresholds);
        System.out.println("Guide checks passed: " + assertions + " assertions.");
    }
    /** Rules/transactions proof, not a substitute for live kill, pickup, tile or UI delivery. */
    private static void defaultLoop(FoundationCatalog catalog, long[] thresholds) {
        var data = new HumanWorldData();
        data.registerCommandPost(BuildingPosition.parse("minecraft:overworld@0,64,0").orElseThrow());
        MissionManager.start(data, catalog, thresholds, "recover_parasite_samples");
        var zombie = ThreatRules.parse(ThreatRules.DEFAULTS).match("minecraft:zombie", ignored -> false).orElseThrow();
        for (int kill = 0; kill < 8; kill++) {
            long points = ProgressionRules.addExact(data.points(), zombie.points());
            data.setProgression(points, ProgressionRules.stageFor(points, thresholds));
        }
        MissionManager.observeSamples(data, catalog, thresholds, 3);
        MissionManager.start(data, catalog, thresholds, "establish_field_command");
        MissionManager.start(data, catalog, thresholds, "stockpile_supplies");
        check(data.supplies() == 20 && data.mission(catalog.mission("stockpile_supplies").id()).orElseThrow().completed(), "real mission rewards fund global stockpile before lab import");
        MissionManager.start(data, catalog, thresholds, "complete_first_research");
        var transfer = BuildingTransactions.transfer(BuildingState.EMPTY, data.supplies(), 10, 2000, true);
        data.setSupplies(transfer.global());
        var lab = transfer.local().target(catalog.research("parasite_samples").id());
        var entry = catalog.research(lab.target());
        check(!lab.researching(), "selecting first research does not start work");
        lab = BuildingTransactions.toggleResearch(lab, data, entry, thresholds).work(12);
        lab = BuildingTransactions.finishResearch(lab, data, entry, thresholds, 12);
        MissionManager.refresh(data, catalog, thresholds);
        check(data.points() == 150 && ProgressionRules.stageFor(data.points(), thresholds).level() == 1, "eight default kill credits plus mission/research rewards reach early army stage without grants");
        check(data.supplies() == 20 && lab.supplies() == 0 && lab.samples() == 0, "local research cost spent once, separate global mission reward retained");
        MissionManager.start(data, catalog, thresholds, "early_army_response");
        check(data.points() == 175 && data.mission(catalog.mission("early_army_response").id()).orElseThrow().completed(), "stage objective observes earned progression");
        var saved = HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var loaded = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        MissionManager.refresh(loaded, catalog, thresholds);
        MissionManager.observeSamples(loaded, catalog, thresholds, 3);
        MissionManager.start(loaded, catalog, thresholds, "early_army_response");
        check(loaded.points() == 175 && loaded.supplies() == 20 && loaded.isResearchComplete(entry.id()), "save/reload and repeated observations preserve one-time transactions");
        check(readOnly(loaded, catalog, thresholds, true).contains("Defend the Outpost"), "guide leads earned progression into real raid defence");
    }
    private static String readOnly(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, boolean operator) {
        data.setDirty(false);
        var before = HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var lines = CommandGuide.lines(data, catalog, thresholds, operator);
        check(lines.size() <= 6, "explicit guide output compact and bounded");
        check(!data.isDirty() && before.equals(HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow()), "guide cannot spend, dirty, refresh, start or complete saved state");
        return String.join("\n", lines);
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
