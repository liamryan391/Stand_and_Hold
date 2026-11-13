package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.threat.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/** M34 Defend the Post: a natural raid is radioed in first, then arrives from the announced side; the defence order runs itself. */
final class DefenceTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    private DefenceTests() { }

    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("defence_warned_raid_arrives", DefenceTests::arrives);
        tests.put("defence_warned_raid_needs_witness", DefenceTests::witness);
        tests.put("defence_order_runs_itself", DefenceTests::order);
        tests.put("defence_warned_raid_natural_off", DefenceTests::naturalOff);
        tests.put("defence_raid_bar", DefenceTests::raidBar);
        return tests;
    }

    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static String key(GameTestHelper h, BuildingBlockEntity post) { return new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()).key(); }
    private static BuildingBlockEntity post(GameTestHelper h) {
        h.setBlock(HOME, BuildingRegistry.COMMAND_POST.get());
        var post = h.getBlockEntity(HOME, BuildingBlockEntity.class);
        post.change(new BuildingState(1, 20, 2, "", 0)); return post;
    }
    private static QaPlayer player(GameTestHelper h, BlockPos relative) {
        var player = QaPlayer.join(h.getLevel()); var pos = h.absolutePos(relative);
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180, 0); return player;
    }
    private static void stage(GameTestHelper h, HumanStage stage) { HumanPointManager.setStage(h.getLevel().getServer(), stage); }
    /** Makes the saved arrival due now (test environments may not advance game time); the octant is kept. */
    private static void arriveNow(GameTestHelper h, BuildingBlockEntity post) {
        var state = ThreatResponse.state(h.getLevel(), post);
        ThreatWorldData.get(h.getLevel().getServer()).putResponse(key(h, post), state.pending(Math.max(1, h.getLevel().getGameTime()), state.bearing()));
    }
    private static java.util.List<Mob> raiders(GameTestHelper h) {
        return h.getLevel().getEntitiesOfClass(Mob.class, new AABB(h.absolutePos(HOME)).inflate(20), mob -> mob.entityTags().contains(ThreatResponse.RAID_TAG));
    }

    private static void arrives(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h); var player = player(h, HOME.offset(0, 0, 3)); stage(h, HumanStage.LOCAL_ARMY_RESPONSE);
        h.runAfterDelay(2, () -> {
            h.assertTrue(ThreatResponse.warnRaid(h.getLevel(), post).startsWith("Raid warned"), "the natural path warns first");
            var warned = ThreatResponse.state(h.getLevel(), post); long now = h.getLevel().getGameTime();
            h.assertTrue(warned.hasPending() && warned.pendingRaid() == now + ResponseConfig.WARNING_TICKS.get() && warned.raiders().isEmpty()
                    && warned.nextRaid() > now, "arrival tick and cooldown saved; nobody spawned yet");
            h.assertTrue(raiders(h).isEmpty(), "no raider exists during the warning");
            h.assertTrue(player.messages("message.standandhold.raid_incoming") == 1, "the nearby player hears the radio warning once");
            h.assertTrue(ThreatResponse.warnRaid(h.getLevel(), post).startsWith("Raid already incoming")
                    && ThreatResponse.raid(h.getLevel(), post).startsWith("Raid already incoming"), "no second wave while one is warned");
            var menu = new BuildingMenu(1, player.getInventory(), post, FoundationResources.get(h.getLevel().getServer()));
            h.assertTrue(menu.number(BuildingMenu.RAID_ETA) > 0 && menu.number(BuildingMenu.RAID_BEARING) == warned.bearing(), "the post menu shows the incoming raid");
            arriveNow(h, post);
            h.runAfterDelay(1, () -> {
                ThreatResponse.tick(h.getLevel(), post);
                var after = ThreatResponse.state(h.getLevel(), post);
                h.assertTrue(!after.hasPending() && !after.raiders().isEmpty() && after.raids() == 1, "the warned wave arrives on the next post check: " + after
                        + " at " + h.getLevel().getGameTime() + ", players " + h.getLevel().players().size() + ", difficulty " + h.getLevel().getDifficulty());
                var center = h.absolutePos(HOME);
                for (var mob : raiders(h)) {
                    double bearing = Math.toDegrees(Math.atan2(mob.getX() - (center.getX() + 0.5), -(mob.getZ() - (center.getZ() + 0.5))));
                    double off = Math.abs(((bearing - warned.bearing() * 45) % 360 + 540) % 360 - 180);
                    h.assertTrue(off <= 45, "raider comes from the announced side: off by " + off + " degrees");
                }
                h.assertTrue(player.messages("message.standandhold.raid_contact") == 1 && player.messages("message.standandhold.raid") == 0,
                        "arrival names the side the raiders actually came from");
                h.succeed();
            });
        });
    }

    private static String barKey(net.minecraft.server.level.ServerBossEvent bar) {
        return bar.getName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text ? text.getKey() : "";
    }
    /** M35: a yellow countdown bar while warned, a red raider bar during the raid, gone when it is over; nearby players only. */
    private static void raidBar(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h); var player = player(h, HOME.offset(0, 0, 3)); stage(h, HumanStage.LOCAL_ARMY_RESPONSE);
        var server = h.getLevel().getServer();
        h.runAfterDelay(2, () -> {
            RaidBars.update(h.getLevel(), post);
            h.assertTrue(RaidBars.bar(server, key(h, post)).isEmpty(), "no bar without a raid");
            ThreatResponse.warnRaid(h.getLevel(), post);
            RaidBars.update(h.getLevel(), post);
            var warning = RaidBars.bar(server, key(h, post)).orElseThrow();
            h.assertTrue(barKey(warning).equals("bossbar.standandhold.raid_warning") && warning.getColor() == net.minecraft.world.BossEvent.BossBarColor.YELLOW
                    && warning.getPlayers().contains(player) && warning.getProgress() > 0 && warning.getProgress() <= 1, "yellow warning bar shown to the nearby player");
            arriveNow(h, post);
            h.runAfterDelay(1, () -> {
                ThreatResponse.tick(h.getLevel(), post);
                RaidBars.update(h.getLevel(), post);
                var raid = RaidBars.bar(server, key(h, post)).orElseThrow();
                h.assertTrue(barKey(raid).equals("bossbar.standandhold.raid") && raid.getColor() == net.minecraft.world.BossEvent.BossBarColor.RED
                        && raid.getProgress() == 1F && raid.getPlayers().contains(player), "red raid bar while raiders are reserved");
                var data = ThreatWorldData.get(server);
                for (var id : ThreatResponse.state(h.getLevel(), post).raiders()) { data.releaseRaider(key(h, post), id); }
                RaidBars.update(h.getLevel(), post);
                h.assertTrue(RaidBars.bar(server, key(h, post)).isEmpty() && raid.getPlayers().isEmpty(), "the bar is removed when the raid is over");
                h.succeed();
            });
        });
    }
    /** Turning natural raids off after a warning clears it at arrival: no wave, no counters; operator raids still work. */
    private static void naturalOff(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h); var player = player(h, HOME.offset(0, 0, 3)); stage(h, HumanStage.LOCAL_ARMY_RESPONSE);
        boolean natural = ResponseConfig.RAIDS.get();
        h.testInfo.addListener(new net.minecraft.gametest.framework.GameTestListener() {
            private void restore() { ResponseConfig.RAIDS.set(natural); }
            public void testStructureLoaded(net.minecraft.gametest.framework.GameTestInfo info) { }
            public void testPassed(net.minecraft.gametest.framework.GameTestInfo info, net.minecraft.gametest.framework.GameTestRunner runner) { restore(); }
            public void testFailed(net.minecraft.gametest.framework.GameTestInfo info, net.minecraft.gametest.framework.GameTestRunner runner) { restore(); }
            public void testAddedForRerun(net.minecraft.gametest.framework.GameTestInfo old, net.minecraft.gametest.framework.GameTestInfo copy, net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
        h.runAfterDelay(2, () -> {
            h.assertTrue(ThreatResponse.warnRaid(h.getLevel(), post).startsWith("Raid warned"), "warned while natural raids were on");
            ResponseConfig.RAIDS.set(false);
            arriveNow(h, post);
            h.runAfterDelay(1, () -> {
                ThreatResponse.tick(h.getLevel(), post);
                var after = ThreatResponse.state(h.getLevel(), post); long now = h.getLevel().getGameTime();
                h.assertTrue(!after.hasPending() && after.raiders().isEmpty() && raiders(h).isEmpty() && after.raids() == 0
                        && player.messages("message.standandhold.raid_contact") == 0, "natural raids off: the warned wave never arrives and nothing counts: " + after);
                ThreatWorldData.get(h.getLevel().getServer()).putResponse(key(h, post), after.timers(0, after.nextReinforcement()));
                h.assertTrue(ThreatResponse.raid(h.getLevel(), post).startsWith("Outpost raid") && !raiders(h).isEmpty(), "operator debug raids stay independent of the natural toggle");
                h.succeed();
            });
        });
    }
    private static void witness(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h); stage(h, HumanStage.LOCAL_ARMY_RESPONSE);
        var player = player(h, HOME.offset(0, 0, 3));
        h.runAfterDelay(2, () -> {
            ThreatResponse.warnRaid(h.getLevel(), post);
            QaPlayer.closeAll();
            arriveNow(h, post);
            h.runAfterDelay(1, () -> {
                ThreatResponse.tick(h.getLevel(), post);
                var after = ThreatResponse.state(h.getLevel(), post); long now = h.getLevel().getGameTime();
                h.assertTrue(!after.hasPending() && after.raiders().isEmpty() && raiders(h).isEmpty(), "nobody near the post: the wave stands down: " + after
                        + " at " + now + ", players " + h.getLevel().players().size() + ", tagged " + raiders(h).size());
                h.assertTrue(after.nextRaid() <= now + ResponseConfig.BACKOFF.get() && after.nextRaid() > now && after.raids() == 0,
                        "a short saved backoff replaces the cooldown; nothing counted");
                h.succeed();
            });
        });
    }

    /** Once Stage 1 completes the onboarding chain, the defence order is current and active with no operator, showing the local threat. */
    private static void order(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h); var player = player(h, HOME.offset(0, 0, 3));
        h.runAfterDelay(2, () -> {
            var server = h.getLevel().getServer(); var catalog = FoundationResources.get(server); var thresholds = HumanProgressionConfig.thresholds();
            for (var id : java.util.List.of("recover_parasite_samples", "establish_field_command", "stockpile_supplies", "complete_first_research", "early_army_response")) {
                MissionManager.start(data(h), catalog, thresholds, id); MissionManager.advance(data(h), catalog, thresholds, id, 1_000_000);
            }
            MissionManager.sync(data(h), catalog, thresholds, -1);
            h.assertTrue(data(h).mission("standandhold:defend_outpost").filter(state -> !state.completed()).isPresent(), "the defence order started itself");
            var menu = new BuildingMenu(1, player.getInventory(), post, catalog);
            h.assertTrue(menu.order() != null && menu.order().id().equals("standandhold:defend_outpost") && menu.number(BuildingMenu.ORDER_STATE) == BuildingMenu.ORDER_ACTIVE,
                    "Orders shows the defence order as Active");
            h.assertTrue(menu.number(BuildingMenu.THREAT_NEED) == ResponseConfig.RAID_THRESHOLD.get() && menu.number(BuildingMenu.RAID_ETA) == -1,
                    "Orders shows the local threat line and no raid yet");
            ThreatResponse.observe(h.getLevel(), post.getBlockPos(), ResponseConfig.KILL_SCORE.get(), true, true, false, false);
            player.containerMenu = menu; menu.broadcastChanges();
            h.assertTrue(menu.number(BuildingMenu.THREAT) == ResponseConfig.KILL_SCORE.get(), "a credited kill near the post raises the shown threat");
            h.succeed();
        });
    }
}
