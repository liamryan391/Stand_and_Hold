package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.common.threat.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

final class SupportTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        return Map.of("support_gates_command", SupportTests::gates, "support_two_players", SupportTests::twoPlayers,
                "support_post_cap", SupportTests::postCap, "support_area_cap", SupportTests::areaCap,
                "support_terrain", SupportTests::terrain, "support_invalid_site", SupportTests::invalid,
                "support_burst_budget", SupportTests::burst);
    }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static BuildingBlockEntity post(GameTestHelper h, BlockPos relative) {
        h.setBlock(relative, BuildingRegistry.COMMAND_POST.get());
        return h.getBlockEntity(relative, BuildingBlockEntity.class);
    }
    private static QaPlayer observer(GameTestHelper h, BlockPos relative) {
        var player = QaPlayer.join(h.getLevel()); var pos = h.absolutePos(relative);
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16);
        player.setYHeadRot(180);
        return player;
    }
    private static void unlock(GameTestHelper h) {
        var server = h.getLevel().getServer(); var catalog = FoundationResources.get(server);
        HumanPointManager.setStage(server, HumanStage.LOCAL_ARMY_RESPONSE); data(h).setSupplies(100);
        ResearchManager.complete(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
        ResearchManager.complete(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:field_communications");
        data(h).setSupplies(100);
    }
    private static void refused(GameTestHelper h, QaPlayer player, BuildingBlockEntity post, String reason) {
        long balance = data(h).supplies();
        try { SupportCallIn.request(player, post); h.fail("Expected refusal: " + reason); }
        catch (IllegalArgumentException failure) { h.assertTrue(failure.getMessage().contains(reason), "Specific refusal: " + failure.getMessage()); }
        h.assertTrue(data(h).supplies() == balance, "Refused action spends nothing");
    }
    private static String key(GameTestHelper h, BuildingBlockEntity post) { return new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()).key(); }
    private static void gates(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME); var player = observer(h, HOME); data(h).setSupplies(100);
        h.runAfterDelay(3, () -> {
            refused(h, player, post, "Stage");
            HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.LOCAL_ARMY_RESPONSE);
            refused(h, player, post, "research");
            unlock(h); data(h).setSupplies(9); refused(h, player, post, "Insufficient supplies"); data(h).setSupplies(100);
            var ray = player.pick(6, 1, false);
            h.assertTrue(ray instanceof net.minecraft.world.phys.BlockHitResult hit && hit.getBlockPos().equals(post.getBlockPos())
                    && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK,
                    "Actual ray selects post " + post.getBlockPos() + "; eye " + player.getEyePosition() + ", look " + player.getLookAngle() + ", hit " + ray.getType() + " " + ray.getLocation());
            h.assertTrue(SupportCallIn.lookedAt(player) == post, "Guarded ray selects the same post");
            h.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "standandhold support reinforce");
            h.assertTrue(data(h).supplies() == 90 && data(h).defenderCount(key(h, post)) == 1, "Non-op real command spawns one unit/spends once");
            var repeat = SupportCallIn.request(player, post);
            h.assertTrue(!repeat.success() && repeat.message().contains("cooldown") && data(h).supplies() == 90, "Repeated request shares cooldown and returns reserved payment");
            h.succeed();
        });
    }
    private static void twoPlayers(GameTestHelper h) {
        QaWorld.begin(h); var a = post(h, HOME); var b = post(h, HOME.offset(12, 0, 0));
        var one = observer(h, HOME); var two = observer(h, HOME.offset(12, 0, 0)); unlock(h); data(h).setSupplies(10);
        h.runAfterDelay(3, () -> {
            h.assertTrue(SupportCallIn.request(one, a).success(), "First player receives support");
            refused(h, two, b, "Insufficient supplies");
            h.assertTrue(data(h).supplies() == 0 && data(h).defenderCount(key(h, a)) == 1 && data(h).defenderCount(key(h, b)) == 0, "Shared bank cannot be spent twice across posts");
            h.succeed();
        });
    }
    private static void postCap(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME); var player = observer(h, HOME); unlock(h);
        h.runAfterDelay(3, () -> {
            var responses = ThreatWorldData.get(h.getLevel().getServer());
            for (int i = 0; i < 3; i++) {
                h.assertTrue(SupportCallIn.request(player, post).success(), "Within post quota " + i);
                var state = ThreatResponse.state(h.getLevel(), post);
                responses.putResponse(key(h, post), state.timers(state.nextRaid(), 0)); // Isolate quota, not a gameplay bypass.
            }
            h.assertTrue(!SupportCallIn.request(player, post).success() && data(h).supplies() == 70, "Fourth unit refused/refunded");
            h.assertTrue(data(h).defenderCount(key(h, post)) == 3, "No extra call-in budget"); h.succeed();
        });
    }
    private static void areaCap(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME); var player = observer(h, HOME); unlock(h);
        for (int i = 0; i < 8; i++) { h.spawn(NpcRegistry.SURVIVOR.get(), HOME.offset(-4 + i % 4, 0, -4 + i / 4)).setNoAi(true); }
        h.runAfterDelay(3, () -> {
            h.assertTrue(!SupportCallIn.request(player, post).success() && data(h).supplies() == 100 && data(h).defenderCount(key(h, post)) == 0, "Egg units count toward shared area cap; no payment on refusal"); h.succeed();
        });
    }
    private static void terrain(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME); var player = observer(h, HOME); unlock(h);
        for (BlockPos pos : BlockPos.betweenClosed(20, -1, 20, 44, 1, 44)) { h.setBlock(pos, Blocks.AIR); }
        // Keep only the observer's support, away from the post's spawn-radius candidates.
        h.setBlock(HOME.offset(0, -1, 4), Blocks.GRASS_BLOCK);
        h.runAfterDelay(3, () -> {
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
            h.assertTrue(!SupportCallIn.request(player, post).success() && data(h).supplies() == 100 && data(h).defenderCount(key(h, post)) == 0, "Unsupported ground refuses without a charge or phantom unit");
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "Request loads no chunks"); h.succeed();
        });
    }
    private static void invalid(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h, HOME); var player = observer(h, HOME); unlock(h);
        h.runAfterDelay(3, () -> {
            player.setGameMode(GameType.SPECTATOR); refused(h, player, post, "spectators"); player.setGameMode(GameType.SURVIVAL);
            var pos = h.absolutePos(HOME.offset(20, 0, 0)); player.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0); refused(h, player, post, "too far");
            pos = h.absolutePos(HOME); player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16);
            post.initializeMainBase(); data(h).registerMainBase(new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()));
            refused(h, player, post, "dormant");
            h.setBlock(HOME, Blocks.AIR); refused(h, player, post, "missing");
            h.assertTrue(ThreatWorldData.get(h.getLevel().getServer()).response(key(h, post)) == null, "No event on stale/dormant post"); h.succeed();
        });
    }
    private static void burst(GameTestHelper h) {
        QaWorld.begin(h); unlock(h); data(h).setSupplies(1000);
        var posts = new BuildingBlockEntity[4]; var players = new QaPlayer[4];
        for (int i = 0; i < 4; i++) {
            var pos = new BlockPos(i % 2 == 0 ? 12 : 52, 2, i < 2 ? 12 : 52);
            posts[i] = post(h, pos); players[i] = observer(h, pos);
        }
        h.runAfterDelay(3, () -> {
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount(), successes = 0;
            for (int round = 0; round < 20; round++) {
                for (int i = 0; i < 4; i++) { if (SupportCallIn.request(players[i], posts[i]).success()) { successes++; } }
            }
            h.assertTrue(successes == 4 && data(h).supplies() == 960, "80 serialized requests from four players charge/deploy four times only");
            for (var post : posts) { h.assertTrue(data(h).defenderCount(key(h, post)) == 1, "Each home keeps one exact reservation"); }
            h.assertTrue(h.getLevel().getEntitiesOfClass(com.liamryan.standandhold.common.npc.HumanNpc.class, h.getBounds()).size() == 4, "Actual entity count matches receipts");
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "Multi-post burst loads no extra chunks"); h.succeed();
        });
    }
}
