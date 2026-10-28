package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.common.threat.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.world.level.block.Blocks;

/** Real shared deployment paths; config overrides are scoped and restored even on assertion failure. */
final class OutpostTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        Map<String, Consumer<GameTestHelper>> cases = Map.of("outpost_upgrade_paths", OutpostTests::upgrade, "outpost_adjacent_cap", OutpostTests::adjacent,
                "outpost_config_limits", OutpostTests::config, "outpost_reservation_retention", OutpostTests::retention,
                "outpost_stage_dormancy_stale", OutpostTests::gates, "outpost_natural_reinforcement", OutpostTests::natural,
                "outpost_paid_upgraded_slot", OutpostTests::paid);
        var scoped = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        cases.forEach((name, test) -> scoped.put(name, h -> {
            int ceiling = NpcConfig.POST_CAP.get(); var profile = NpcConfig.POST_LEVELS.get();
            // An older retained QA config may explicitly cap posts at three. Test fresh-config growth
            // without modifying that file; separate cases assert the legacy ceiling still wins.
            NpcConfig.POST_CAP.set(16); NpcConfig.POST_LEVELS.set(DefenderCapacity.DEFAULT_LEVELS);
            h.testInfo.addListener(new GameTestListener() {
                private void restore() { NpcConfig.POST_CAP.set(ceiling); NpcConfig.POST_LEVELS.set(profile); }
                public void testStructureLoaded(GameTestInfo info) { }
                public void testPassed(GameTestInfo info, GameTestRunner runner) { restore(); }
                public void testFailed(GameTestInfo info, GameTestRunner runner) { restore(); }
                public void testAddedForRerun(GameTestInfo old, GameTestInfo copy, GameTestRunner runner) { }
            });
            test.accept(h);
        }));
        return scoped;
    }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static String key(GameTestHelper h, BuildingBlockEntity post) { return new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()).key(); }
    private static BuildingBlockEntity post(GameTestHelper h, BlockPos pos, int level) {
        h.setBlock(pos, BuildingRegistry.COMMAND_POST.get());
        var post = h.getBlockEntity(pos, BuildingBlockEntity.class);
        post.change(new BuildingState(level, 20, 2, "", 0)); return post;
    }
    private static QaPlayer caller(GameTestHelper h) {
        var player = QaPlayer.join(h.getLevel()); var pos = h.absolutePos(HOME);
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16); player.setYHeadRot(180); return player;
    }
    private static void unlock(GameTestHelper h) {
        var server = h.getLevel().getServer(); var catalog = FoundationResources.get(server);
        data(h).setSupplies(200); HumanPointManager.setStage(server, HumanStage.LOCAL_ARMY_RESPONSE);
        ResearchManager.complete(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
        ResearchManager.complete(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:field_communications");
        data(h).setSupplies(200);
    }
    private static HumanNpc deploy(GameTestHelper h, BuildingBlockEntity post) {
        var unit = DefenderDeployment.attempt(h.getLevel(), post);
        h.assertTrue(unit != null, "Safe arena has a slot at level " + post.state().level());
        unit.setNoAi(true); return unit;
    }
    private static void clearCooldown(GameTestHelper h, BuildingBlockEntity post) {
        var state = ThreatResponse.state(h.getLevel(), post);
        ThreatWorldData.get(h.getLevel().getServer()).putResponse(key(h, post), state.timers(state.nextRaid(), 0));
    }
    private static void menu(GameTestHelper h, BuildingBlockEntity post, QaPlayer player, int assigned, int cap, int reason) {
        var menu = new BuildingMenu(1, player.getInventory(), post, FoundationResources.get(h.getLevel().getServer()));
        h.assertTrue(menu.number(BuildingMenu.DEFENDERS) == assigned && menu.number(BuildingMenu.DEFENDER_CAP) == cap
                && menu.number(BuildingMenu.DEPLOYMENT_STATUS) == reason, "Authoritative status: " + assigned + "/" + cap + " reason " + reason);
    }
    private static void upgrade(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME, 1); var player = caller(h); unlock(h);
        h.runAfterDelay(3, () -> {
            var first = deploy(h, post); deploy(h, post); deploy(h, post);
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Level 1 still stops at three");
            menu(h, post, player, 3, 3, 4);
            var menu = new BuildingMenu(2, player.getInventory(), post, FoundationResources.get(h.getLevel().getServer()));
            player.containerMenu = menu;
            long points = data(h).points();
            h.assertTrue(menu.clickMenuButton(player, BuildingMenu.UPGRADE), "Real server-authoritative upgrade action");
            h.assertTrue(post.state().level() == 2 && post.state().supplies() == 0 && post.state().samples() == 2
                    && data(h).points() == points && first.isAlive(), "Existing upgrade costs/state unchanged; old unit retained");
            menu(h, post, player, 3, 5, 0);
            // Drive the same tile ticker/interval as normal automatic deployment, not a direct spawn shortcut.
            for (int i = 0; i < NpcConfig.INTERVAL.get(); i++) { BuildingBlockEntity.tick(h.getLevel(), post.getBlockPos(), post.getBlockState(), post); }
            h.assertTrue(data(h).defenderCount(key(h, post)) == 4, "Automatic interval uses upgraded slot");
            h.assertTrue(ThreatResponse.deployReinforcement(h.getLevel(), post).success(), "Event path fills fifth slot");
            clearCooldown(h, post); long balance = data(h).supplies();
            h.assertTrue(!SupportCallIn.request(player, post).success() && data(h).supplies() == balance, "Paid path cannot bypass full upgraded quota; refund exact");
            h.assertTrue(data(h).defenderCount(key(h, post)) == 5, "One shared roster across deployment paths");
            menu(h, post, player, 5, 5, 4); h.succeed();
        });
    }
    private static void adjacent(GameTestHelper h) {
        QaWorld.begin(h); var a = post(h, HOME, 2); var b = post(h, HOME.offset(12, 0, 0), 5); unlock(h);
        h.runAfterDelay(3, () -> {
            for (int i = 0; i < 5; i++) { deploy(h, a); }
            for (int i = 0; i < 3; i++) { deploy(h, b); }
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), b) == null && !ThreatResponse.deployReinforcement(h.getLevel(), b).success(), "Adjacent posts share eight loaded humans even with spare level-5 slots");
            var player = QaPlayer.join(h.getLevel()); var pos = b.getBlockPos(); player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16);
            clearCooldown(h, b); long balance = data(h).supplies();
            h.assertTrue(!SupportCallIn.request(player, b).success() && data(h).supplies() == balance, "Area refusal conserves payment");
            h.assertTrue(data(h).defenderCount(key(h, a)) == 5 && data(h).defenderCount(key(h, b)) == 3, "Actual adjacent rosters total eight");
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "No extra chunks requested"); h.succeed();
        });
    }
    private static void paid(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME, 2); var player = caller(h); unlock(h);
        h.runAfterDelay(3, () -> {
            for (int i = 0; i < 4; i++) { deploy(h, post); }
            h.assertTrue(SupportCallIn.request(player, post).success() && data(h).supplies() == 190
                    && data(h).defenderCount(key(h, post)) == 5, "Paid call-in can use fifth slot, with exactly one debit");
            var repeat = SupportCallIn.request(player, post);
            h.assertTrue(!repeat.success() && repeat.message().contains("cooldown") && data(h).supplies() == 190, "Upgraded post still shares real reinforcement cooldown");
            h.succeed();
        });
    }
    private static void config(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME, 5); var player = caller(h); unlock(h);
        h.runAfterDelay(3, () -> {
            int ceiling = NpcConfig.POST_CAP.get(); var profile = NpcConfig.POST_LEVELS.get(); boolean enabled = NpcConfig.DEPLOY.get();
            try {
                NpcConfig.POST_CAP.set(3);
                for (int i = 0; i < 3; i++) { deploy(h, post); }
                menu(h, post, player, 3, 3, 4);
                h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Legacy explicit three remains absolute at level 5");
                NpcConfig.POST_CAP.set(0); menu(h, post, player, 3, 0, 1);
                long balance = data(h).supplies();
                h.assertTrue(!SupportCallIn.request(player, post).success() && data(h).supplies() == balance, "Legacy zero blocks/refunds paid support");
                NpcConfig.POST_CAP.set(16); NpcConfig.DEPLOY.set(false); menu(h, post, player, 3, 8, 1);
                h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Master disable prevents deployment");
                NpcConfig.DEPLOY.set(true); NpcConfig.POST_LEVELS.set(List.of(0, 0, 0, 0, 0));
                h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Per-level zero disables");
                NpcConfig.POST_LEVELS.set(List.of(3, 2, 6, 7, 8));
                h.assertTrue(!DefenderCapacity.validProfile(NpcConfig.POST_LEVELS.get()) && NpcConfig.capacity(5) == 8, "Malformed in-memory profile uses bounded documented fallback");
                NpcConfig.POST_CAP.set(3); menu(h, post, player, 3, 3, 4);
                h.assertTrue(data(h).defenderCount(key(h, post)) == 3, "Config changes retain all reservations");
            } finally { NpcConfig.POST_CAP.set(ceiling); NpcConfig.POST_LEVELS.set(profile); NpcConfig.DEPLOY.set(enabled); }
            h.succeed();
        });
    }
    private static void retention(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME, 2); var player = caller(h); unlock(h);
        h.runAfterDelay(3, () -> {
            var units = new ArrayList<HumanNpc>(); for (int i = 0; i < 4; i++) { units.add(deploy(h, post)); }
            UUID unloaded = UUID.randomUUID();
            h.assertTrue(data(h).reserveDefender(key(h, post), unloaded, 5), "Represent assigned defender whose entity chunk is not loaded");
            int ceiling = NpcConfig.POST_CAP.get();
            try {
                NpcConfig.POST_CAP.set(2); HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.SURVIVORS);
                menu(h, post, player, 5, 2, 4);
                h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Lower ceiling cannot replace unloaded/over-cap defenders");
                for (var unit : units) { h.assertTrue(unit.isAlive() && h.getLevel().getEntity(unit.getUUID()) == unit, "No existing unit deleted/replaced on stage or cap drop"); }
                h.assertTrue(data(h).defenderCount(key(h, post)) == 5, "Unloaded reservation is not cleaned to manufacture room");
            } finally { NpcConfig.POST_CAP.set(ceiling); }
            h.succeed();
        });
    }
    private static void gates(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME, 5); var player = caller(h); unlock(h);
        h.runAfterDelay(3, () -> {
            int survivor = NpcConfig.SURVIVOR.stage.get(), army = NpcConfig.ARMY.stage.get();
            try {
                HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.SURVIVORS);
                NpcConfig.SURVIVOR.stage.set(2); NpcConfig.ARMY.stage.set(3);
                menu(h, post, player, 0, 8, 3);
                h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Post level never bypasses configured world stage gates");
            } finally { NpcConfig.SURVIVOR.stage.set(survivor); NpcConfig.ARMY.stage.set(army); }
            post.initializeMainBase(); data(h).registerMainBase(new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()));
            menu(h, post, player, 0, 8, 2);
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Dormant base gets no units despite high building level");
            h.setBlock(HOME, Blocks.AIR); BuildingPositions.cleanLoaded(h.getLevel(), data(h));
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null && !data(h).commandPostKeys().contains(key(h, post)), "Stale removed tile cannot deploy");
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "Cleanup/invalid attempt does not load chunks"); h.succeed();
        });
    }
    private static void natural(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME, 2); unlock(h);
        h.runAfterDelay(3, () -> {
            for (int i = 0; i < 4; i++) { deploy(h, post); }
            var region = ThreatResponse.patrolRegion(h.getLevel(), post.getBlockPos());
            ThreatResponse.observe(h.getLevel(), region.center(post.getBlockPos().getY()), ResponseConfig.REINFORCEMENT_THRESHOLD.get(), false, false, false, true);
            var state = ThreatResponse.state(h.getLevel(), post);
            ThreatWorldData.get(h.getLevel().getServer()).putResponse(key(h, post), new ResponseRecord(0, 0, 0, 0, state.raiders(), 0));
            ThreatResponse.tick(h.getLevel(), post);
            h.assertTrue(data(h).defenderCount(key(h, post)) == 5 && ThreatResponse.state(h.getLevel(), post).reinforcements() == 1, "Natural response uses fifth upgraded slot and saves receipt");
            ThreatResponse.tick(h.getLevel(), post);
            h.assertTrue(data(h).defenderCount(key(h, post)) == 5, "Natural repeat respects cooldown/quota"); h.succeed();
        });
    }
}
