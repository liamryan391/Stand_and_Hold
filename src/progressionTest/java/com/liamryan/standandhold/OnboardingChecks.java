package com.liamryan.standandhold;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingState;
import com.liamryan.standandhold.common.building.BuildingTransactions;
import com.liamryan.standandhold.common.building.PostSpacing;
import com.liamryan.standandhold.common.compat.ThreatPresets;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.mission.MissionManager.Change;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.structure.StructureKind;
import com.liamryan.standandhold.common.structure.StructurePlan;
import com.liamryan.standandhold.common.threat.ThreatRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.ThreatConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;

/** M32: earned Survival onboarding rules. Logic proof only; live hooks, menus and placement are GameTest/qaClient work. */
final class OnboardingChecks {
    private static int assertions;
    private static final long[] THRESHOLDS = ProgressionRules.thresholds(ProgressionRules.DEFAULT_THRESHOLDS);
    private static final BuildingPosition POST = BuildingPosition.parse("minecraft:overworld@0,64,0").orElseThrow();

    static void run() throws Exception {
        var catalog = FoundationChecks.defaults();
        definitions(catalog);
        autoStart(catalog);
        preCarriedAndShared(catalog);
        earlyImportRecovers(catalog);
        earnedStageOne(catalog);
        boundedAndSafe(catalog);
        spacing();
        pacing(catalog);
        System.out.println("Onboarding checks passed: " + assertions + " assertions.");
    }

    private static void definitions(FoundationCatalog catalog) {
        var flagged = catalog.missions().values().stream().filter(Mission::autoStart).map(Mission::id).toList();
        check(flagged.equals(List.of("standandhold:recover_parasite_samples", "standandhold:establish_field_command", "standandhold:stockpile_supplies",
                "standandhold:complete_first_research", "standandhold:early_army_response")), "exactly the five onboarding orders auto-start, in order");
        String base = "{\"format\":1,\"missions\":[{\"id\":\"pack:a\",\"title\":\"A\",\"objective\":\"supply_total\"";
        check(!FoundationCatalog.parse(base + "}]}").mission("pack:a").autoStart(), "absent flag means operator-started");
        check(FoundationCatalog.parse(base + ",\"auto_start\":true}]}").mission("pack:a").autoStart(), "explicit flag parsed");
        check(!FoundationCatalog.parse(base + ",\"auto_start\":false}]}").mission("pack:a").autoStart(), "explicit false parsed");
        for (String bad : List.of("\"true\"", "1", "null", "[]")) {
            FoundationChecks.expect(() -> FoundationCatalog.parse(base + ",\"auto_start\":" + bad + "}]}"));
            assertions++;
        }
    }

    private static void autoStart(FoundationCatalog catalog) {
        var data = new HumanWorldData();
        var first = MissionManager.sync(data, catalog, THRESHOLDS, -1);
        check(first.size() == 1 && first.get(0).change() == Change.STARTED && first.get(0).mission().id().equals("standandhold:recover_parasite_samples"),
                "fresh world starts only the first order");
        check(data.points() == 0 && data.supplies() == 0, "starting pays nothing");
        data.setDirty(false);
        check(MissionManager.sync(data, catalog, THRESHOLDS, -1).isEmpty() && !data.isDirty(), "repeated sync is an idempotent no-op");
        check(MissionManager.sync(data, catalog, THRESHOLDS, 2).size() == 1 && data.mission("standandhold:recover_parasite_samples").orElseThrow().progress() == 2,
                "carried samples report progress");
        check(MissionManager.sync(data, catalog, THRESHOLDS, 1).isEmpty() && data.mission("standandhold:recover_parasite_samples").orElseThrow().progress() == 2,
                "dropping samples never lowers recorded progress");
        check(data.mission("standandhold:defend_outpost").isEmpty() && data.mission("standandhold:establish_field_command").isEmpty(),
                "later orders wait for prerequisites; operator-only orders never auto-start");
        check(MissionManager.current(data, catalog).orElseThrow().id().equals("standandhold:recover_parasite_samples"), "current order is the active one");
    }

    private static void preCarriedAndShared(FoundationCatalog catalog) {
        var data = new HumanWorldData();
        data.registerCommandPost(POST);
        var notices = MissionManager.sync(data, catalog, THRESHOLDS, 3);
        var completed = notices.stream().filter(n -> n.change() == Change.COMPLETED).map(n -> n.mission().id()).toList();
        check(completed.equals(List.of("standandhold:recover_parasite_samples", "standandhold:establish_field_command", "standandhold:stockpile_supplies")),
                "samples carried before the order started count once and the chain resolves in one bounded sync");
        check(notices.stream().filter(n -> n.change() == Change.STARTED).map(n -> n.mission().id()).toList().equals(List.of("standandhold:complete_first_research")),
                "an order completed on start is reported once as complete, never also as started");
        check(data.points() == 75 && data.supplies() == 20, "three onboarding rewards applied exactly once");
        for (int player = 0; player < 4; player++) {
            check(MissionManager.sync(data, catalog, THRESHOLDS, 3).isEmpty(), "a second player observing the same shared order gets no second reward");
        }
        check(data.points() == 75 && data.supplies() == 20, "shared receipts hold across repeated observations");
    }

    private static void earlyImportRecovers(FoundationCatalog catalog) {
        var data = new HumanWorldData();
        MissionManager.sync(data, catalog, THRESHOLDS, 3);
        check(data.supplies() == 10 && data.mission("standandhold:establish_field_command").orElseThrow().progress() == 0, "no post yet: Establish waits");
        var lab = BuildingTransactions.transfer(BuildingState.EMPTY, data.supplies(), 10, 2000, true);
        data.setSupplies(lab.global());
        data.registerCommandPost(POST);
        MissionManager.sync(data, catalog, THRESHOLDS, -1);
        var stockpile = data.mission("standandhold:stockpile_supplies").orElseThrow();
        check(!stockpile.completed() && stockpile.progress() == 10 && data.supplies() == 10, "early import leaves Stockpile at 10/20, not lost");
        var back = BuildingTransactions.transfer(lab.local(), data.supplies(), 10, 2000, false);
        data.setSupplies(back.global());
        var notices = MissionManager.sync(data, catalog, THRESHOLDS, -1);
        check(notices.stream().anyMatch(n -> n.change() == Change.COMPLETED && n.mission().id().equals("standandhold:stockpile_supplies"))
                && back.local().supplies() == 0 && data.supplies() == 20, "exporting the imported batch recovers the order immediately");
    }

    private static void earnedStageOne(FoundationCatalog catalog) {
        var data = new HumanWorldData();
        var zombie = ThreatRules.parse(ThreatRules.DEFAULTS).match("minecraft:zombie", ignored -> false).orElseThrow();
        for (int kill = 0; kill < 8; kill++) {
            long points = ProgressionRules.addExact(data.points(), zombie.points());
            data.setProgression(points, ProgressionRules.stageFor(points, THRESHOLDS));
            MissionManager.sync(data, catalog, THRESHOLDS, kill >= 7 ? 3 : -1);
        }
        data.registerCommandPost(POST);
        MissionManager.sync(data, catalog, THRESHOLDS, -1);
        var transfer = BuildingTransactions.transfer(BuildingState.EMPTY, data.supplies(), 10, 2000, true);
        data.setSupplies(transfer.global());
        var entry = catalog.research("parasite_samples");
        var lab = BuildingTransactions.toggleResearch(transfer.local().target(entry.id()), data, entry, THRESHOLDS).work(12);
        BuildingTransactions.finishResearch(lab, data, entry, THRESHOLDS, 12);
        var notices = MissionManager.sync(data, catalog, THRESHOLDS, -1);
        check(notices.stream().filter(n -> n.change() == Change.COMPLETED).map(n -> n.mission().id()).toList()
                .equals(List.of("standandhold:complete_first_research", "standandhold:early_army_response")), "first research and Stage 1 complete without an operator");
        check(data.points() == 175 && ProgressionRules.stageFor(data.points(), THRESHOLDS).level() == 1, "eight credited kills plus earned rewards reach Stage 1");
        check(data.mission("standandhold:defend_outpost").isEmpty() && MissionManager.current(data, catalog).orElseThrow().id().equals("standandhold:defend_outpost"),
                "the defence order is next but stays operator-started in M32");
        var reloaded = FoundationChecks.decode(FoundationChecks.encode(data));
        long points = reloaded.points(), supplies = reloaded.supplies();
        check(MissionManager.sync(reloaded, catalog, THRESHOLDS, 3).isEmpty() && reloaded.points() == points && reloaded.supplies() == supplies,
                "save/reload then sync cannot repeat any reward");
    }

    private static void boundedAndSafe(FoundationCatalog catalog) {
        Mission later = new Mission("pack:later", "Later", Mission.Objective.SUPPLY_TOTAL, 1, "", List.of("pack:first"), new FoundationReward(1, 0), true);
        Mission first = new Mission("pack:first", "First", Mission.Objective.SUPPLY_TOTAL, 1, "", List.of(), new FoundationReward(1, 0), true);
        var reversed = new FoundationCatalog(List.of(), List.of(later, first));
        var data = new HumanWorldData();
        data.setSupplies(5);
        var notices = MissionManager.sync(data, reversed, THRESHOLDS, -1);
        check(notices.size() == 2 && data.points() == 2, "dependent orders listed before their prerequisite still resolve within the pass bound");
        var future = new CompoundTag();
        future.putInt("data_version", HumanWorldData.CURRENT_VERSION + 1);
        var unsupported = HumanWorldData.CODEC.parse(NbtOps.INSTANCE, future).getOrThrow();
        FoundationChecks.expect(() -> MissionManager.sync(unsupported, catalog, THRESHOLDS, 3));
        assertions++;
        var saved = FoundationChecks.encode(new HumanWorldData());
        var missions = new CompoundTag();
        missions.putString("standandhold:recover_parasite_samples", "broken");
        saved.put("missions", missions);
        var broken = FoundationChecks.decode(saved);
        var blocked = MissionManager.sync(broken, catalog, THRESHOLDS, 3);
        var blockedIds = blocked.stream().map(n -> n.mission().id()).toList();
        check(!blocked.isEmpty() && blocked.stream().allMatch(n -> n.change() == Change.BLOCKED) && blockedIds.contains("standandhold:recover_parasite_samples")
                && blockedIds.size() == blockedIds.stream().distinct().count(), "a malformed saved order is reported once per order, never repeated per pass");
        check(broken.points() == 0 && broken.supplies() == 0, "nothing is rewarded past a malformed receipt");
        check(FoundationChecks.encode(broken).getCompoundOrEmpty("missions").getStringOr("standandhold:recover_parasite_samples", "").equals("broken"),
                "malformed order data is retained, not replaced");
        FoundationChecks.expect(() -> MissionManager.sync(new HumanWorldData(), catalog, THRESHOLDS, -2));
        assertions++;
    }

    private static void spacing() {
        var overworld = Identifier.parse("minecraft:overworld");
        var keys = List.of("minecraft:overworld@100,64,0", "minecraft:the_nether@1,64,0", "broken key", "minecraft:overworld@0,64,0");
        var near = PostSpacing.nearest(keys, overworld, new BlockPos(30, 70, 40));
        check(near.isPresent() && Math.abs(near.getAsDouble() - 50) < 1e-9, "horizontal distance to the nearest same-dimension post; height ignored");
        check(PostSpacing.nearest(keys, overworld, new BlockPos(0, 64, 0)).getAsDouble() == 100, "a stale entry at the placement position is not counted against itself");
        check(PostSpacing.nearest(List.of("minecraft:the_nether@0,64,0"), overworld, BlockPos.ZERO).isEmpty(), "other dimensions never block");
        var data = new HumanWorldData();
        data.registerCommandPost(POST);
        check(PostSpacing.tooClose(data, overworld, new BlockPos(60, 64, 60), 96).getAsInt() == 84, "84 blocks is refused at the default 96");
        check(PostSpacing.tooClose(data, overworld, new BlockPos(96, 64, 0), 96).isEmpty(), "exactly the configured spacing is allowed");
        check(PostSpacing.tooClose(data, overworld, new BlockPos(5, 64, 0), 0).isEmpty(), "spacing 0 disables the rule");
        check(PostSpacing.tooClose(new HumanWorldData(), overworld, BlockPos.ZERO, 96).isEmpty(), "the first post is always allowed");
    }

    /** 32.1 measurement and pacing guard. Prints numbers for the roadmap; fails only if the default path stops being earnable. */
    private static void pacing(FoundationCatalog catalog) {
        var zombie = ThreatRules.parse(ThreatRules.DEFAULTS).match("minecraft:zombie", ignored -> false).orElseThrow();
        long rewards = catalog.missions().values().stream().filter(Mission::autoStart).filter(m -> m.objective() != Mission.Objective.HUMAN_STAGE)
                .mapToLong(m -> m.reward().points()).sum() + catalog.research("parasite_samples").reward().points();
        long killsForStage = Math.max(0, (THRESHOLDS[1] - rewards + zombie.points() - 1) / zombie.points());
        double killsForSamples = catalog.mission("recover_parasite_samples").target() / (zombie.chance() * zombie.samples());
        check(rewards == 110 && killsForStage == 8, "default onboarding rewards leave eight credited kills for Stage 1");
        check(killsForSamples > 8 && killsForSamples < 9, "about nine kills for three samples, so the two goals arrive together");
        check(ThreatConfig.DEFAULT_PRESETS.equals(List.of("vanilla_undead"))
                && ThreatPresets.resolve(ThreatRules.DEFAULTS, ThreatConfig.DEFAULT_PRESETS, id -> null).rules().match("minecraft:husk",
                        tag -> tag.equals("standandhold:vanilla_undead")).isPresent(), "husks count by default");
        var zombieRule = ThreatPresets.resolve(ThreatRules.DEFAULTS, ThreatConfig.DEFAULT_PRESETS, id -> null).rules().match("minecraft:zombie", tag -> true).orElseThrow();
        check(!zombieRule.tag() && zombieRule.points() == 5, "zombies keep the exact manual rule, so they are credited once");
        StringBuilder report = new StringBuilder("Checkpoint candidates within 512 blocks of 0,0 (before terrain checks):");
        for (long seed : new long[] {170310L, 918273L, -4172144997902289642L}) {
            int candidates = 0, nearest = Integer.MAX_VALUE;
            for (int x = -32; x <= 32; x++) {
                for (int z = -32; z <= 32; z++) {
                    if (x * x + z * z > 32 * 32 || !StructurePlan.candidate(seed, x, z, 12, 2, StructureKind.CHECKPOINT.salt())) { continue; }
                    candidates++;
                    nearest = Math.min(nearest, (int) Math.round(Math.sqrt(x * x + z * z) * 16));
                }
            }
            check(candidates <= (int) Math.ceil(Math.PI * 32 * 32 / 144) + 8, "at most one candidate per 12x12 region");
            report.append(" seed ").append(seed).append(": ").append(candidates).append(" (nearest ~").append(nearest).append(" blocks);");
        }
        System.out.println(report);
    }

    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
