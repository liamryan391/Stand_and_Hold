package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

final class GrowthTests {
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("growth_menu_manual_once", GrowthTests::manual);
        tests.put("growth_automatic_paid_level", GrowthTests::automatic);
        tests.put("growth_obstruction_pause_retry", GrowthTests::blocked);
        tests.put("growth_entity_pause_manual", GrowthTests::entity);
        tests.put("growth_future_policy_retained", GrowthTests::future);
        tests.put("growth_interrupted_no_replay", GrowthTests::interrupted);
        tests.put("growth_commands_permissions", GrowthTests::commands);
        tests.put("growth_disabled_old_and_base", GrowthTests::gates);
        tests.put("growth_real_chunk_unload_reload", GrowthTests::unload);
        tests.put("growth_newer_global_schema", GrowthTests::newerGlobal);
        var result = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.forEach((name, test) -> result.put(name, h -> {
            boolean expansion = StructureConfig.EXPANSION.get(), deploy = NpcConfig.DEPLOY.get(), raids = ResponseConfig.RAIDS.get(), reinforcement = ResponseConfig.REINFORCEMENTS.get();
            StructureConfig.EXPANSION.set(true); NpcConfig.DEPLOY.set(false); ResponseConfig.RAIDS.set(false); ResponseConfig.REINFORCEMENTS.set(false);
            h.testInfo.addListener(new GameTestListener() {
                private void restore() { StructureConfig.EXPANSION.set(expansion); NpcConfig.DEPLOY.set(deploy); ResponseConfig.RAIDS.set(raids); ResponseConfig.REINFORCEMENTS.set(reinforcement); }
                public void testStructureLoaded(GameTestInfo info) { }
                public void testPassed(GameTestInfo info, GameTestRunner runner) { restore(); }
                public void testFailed(GameTestInfo info, GameTestRunner runner) { restore(); }
                public void testAddedForRerun(GameTestInfo old, GameTestInfo copy, GameTestRunner runner) { }
            }); test.accept(h);
        })); return result;
    }
    private record Fixture(BuildingBlockEntity post, QaPlayer player, BuildingMenu menu) { }
    private static Fixture begin(GameTestHelper h, int level) {
        QaWorld.begin(h); var center = h.absolutePos(new BlockPos(32, 2, 32)); int radius = StructureConfig.CHECKPOINT_RADIUS.get();
        StructurePlacement.Result result;
        try { StructureConfig.CHECKPOINT_RADIUS.set(4); result = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, center.getX() >> 4, center.getZ() >> 4); }
        finally { StructureConfig.CHECKPOINT_RADIUS.set(radius); }
        h.assertTrue(result.success(), result.message());
        var post = (BuildingBlockEntity) h.getLevel().getBlockEntity(result.commandPost()); post.change(new BuildingState(level, 20, 2, "", 0));
        var player = QaPlayer.join(h.getLevel()); var pos = post.getBlockPos(); player.snapTo(pos.getX() + .5, pos.getY(), pos.getZ() + 4.5, 180, 16); player.setYHeadRot(180);
        var menu = new BuildingMenu(1, player.getInventory(), post, FoundationResources.get(h.getLevel().getServer())); player.containerMenu = menu;
        return new Fixture(post, player, menu);
    }
    private static void drive(GameTestHelper h, BuildingBlockEntity post, int count) {
        for (int i = 0; i < count; i++) { BuildingBlockEntity.tick(h.getLevel(), post.getBlockPos(), post.getBlockState(), post); }
    }
    private static void clean(GameTestHelper h, BuildingBlockEntity post) { h.getLevel().getChunkAt(post.getBlockPos()).tryMarkSaved(); }
    private static boolean dirty(GameTestHelper h, BuildingBlockEntity post) { return h.getLevel().getChunkAt(post.getBlockPos()).isUnsaved(); }
    private static void refused(GameTestHelper h, Runnable action) {
        try { action.run(); h.fail("Expected growth refusal"); } catch (IllegalArgumentException | IllegalStateException expected) { }
    }
    private static void applied(GameTestHelper h, BuildingBlockEntity post) {
        var operation = post.siteOperation().orElseThrow();
        h.assertTrue(post.site().appliedRevision() == 1 && operation.phase() == ExpansionOperation.Phase.APPLIED
                && operation.observe(pos -> NbtUtils.writeBlockState(h.getLevel().getBlockState(pos))).state() == ExpansionOperation.Observed.ALL_AFTER, "Exact existing 137-cell apron applied once");
    }
    private static void manual(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            var data = HumanWorldData.get(h.getLevel().getServer()); var global = HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
            var state = f.post().state(); var before = OutpostExpansion.protectedDigest(h.getLevel(), f.post(), f.post().site()); int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
            var other = QaPlayer.join(h.getLevel());
            h.assertFalse(f.menu().clickMenuButton(other, BuildingMenu.EXPAND), "Foreign player cannot invoke this menu");
            f.player().setGameMode(GameType.SPECTATOR); h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Spectator refused"); f.player().setGameMode(GameType.SURVIVAL);
            var pos = f.player().position(); f.player().snapTo(pos.x + 20, pos.y, pos.z, 0, 0);
            h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Out-of-range action refused"); f.player().snapTo(pos.x, pos.y, pos.z, 180, 16);
            h.assertTrue(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Non-op survival player can expand through real menu action"); applied(h, f.post());
            h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Duplicate packet cannot repeat expansion");
            h.assertTrue(f.player().messages("message.standandhold.growth_complete") == 1 && f.post().state().equals(state)
                    && global.equals(HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow()) && chunks == h.getLevel().getChunkSource().getLoadedChunksCount()
                    && before.equals(OutpostExpansion.protectedDigest(h.getLevel(), f.post(), f.post().site())), "Once-only feedback, unchanged protected buildings/resources/caps/chunks");
            f.post().change(f.post().state().supplies(7)); f.menu().broadcastChanges();
            h.assertTrue(f.menu().number(BuildingMenu.GROWTH_STATUS) == OutpostGrowth.EXPANDED, "Normal storage change does not turn Expanded into a repair problem");
            var broken = f.post().siteOperation().orElseThrow().changes().getFirst().pos(); h.getLevel().setBlockAndUpdate(broken, Blocks.AIR.defaultBlockState());
            drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get()); h.assertTrue(h.getLevel().getBlockState(broken).isAir(), "No automatic repair"); h.succeed();
        });
    }
    private static void automatic(GameTestHelper h) {
        var f = begin(h, 1); h.runAfterDelay(3, () -> {
            h.assertTrue(f.menu().clickMenuButton(f.player(), BuildingMenu.GROWTH_AUTOMATIC), "Explicit opt-in at level 1");
            clean(h, f.post()); h.assertTrue(f.menu().clickMenuButton(f.player(), BuildingMenu.GROWTH_AUTOMATIC) && !dirty(h, f.post()), "Duplicate selection is idempotent/no dirty spam");
            drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get()); h.assertTrue(f.post().siteOperation().isEmpty() && f.post().state().level() == 1, "Automatic mode does not grant/spend for a level");
            var data = HumanWorldData.get(h.getLevel().getServer()); data.setSupplies(10); HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.LOCAL_ARMY_RESPONSE);
            ResearchManager.complete(data, FoundationResources.get(h.getLevel().getServer()), HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
            long points = data.points(), supplies = data.supplies();
            h.assertTrue(f.menu().clickMenuButton(f.player(), BuildingMenu.UPGRADE) && f.post().state().supplies() == 0, "Normal upgrade pays exactly 20 local supplies");
            var pos = f.post().getBlockPos(); f.player().snapTo(pos.getX() + 20, pos.getY(), pos.getZ(), 0, 0);
            drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get()); applied(h, f.post());
            var id = f.post().siteOperation().orElseThrow().id(); clean(h, f.post()); drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
            h.assertTrue(f.post().siteOperation().orElseThrow().id().equals(id) && !dirty(h, f.post()) && data.points() == points && data.supplies() == supplies
                    && f.post().state().level() == 2 && f.post().state().samples() == 2, "Automatic growth is player-independent, once-only, no extra debit/reward/dirty spam"); h.succeed();
        });
    }
    private static void blocked(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            h.assertTrue(f.menu().clickMenuButton(f.player(), BuildingMenu.GROWTH_AUTOMATIC), "Opt in");
            var pos = f.post().site().center().offset(-6, 1, -6); h.getLevel().setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
            drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
            h.assertTrue(f.post().growthSettings().pause() == OutpostGrowth.TERRAIN && f.post().siteOperation().isEmpty() && h.getLevel().getBlockState(pos).is(Blocks.CHEST), "Obstruction is protected and pause saved before writes");
            h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()); clean(h, f.post()); drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
            h.assertTrue(f.post().siteOperation().isEmpty() && !dirty(h, f.post()), "Clearing obstruction does not cause a hidden retry loop");
            h.assertTrue(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Explicit retry uses same manual action"); applied(h, f.post());
            h.assertTrue(f.post().growthSettings().pause() == 0 && f.post().growthSettings().mode() == OutpostGrowth.Mode.AUTOMATIC, "Retry retains preference and clears pause"); h.succeed();
        });
    }
    private static void entity(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            OutpostGrowth.setMode(f.player(), f.post(), OutpostGrowth.Mode.AUTOMATIC); var pos = f.post().site().center().offset(-6, 1, -6);
            var pig = net.minecraft.world.entity.EntityTypes.PIG.create(h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            pig.snapTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0); pig.setNoAi(true); h.getLevel().addFreshEntity(pig);
            drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
            h.assertTrue(f.post().growthSettings().pause() == OutpostGrowth.ENTITY && pig.isAlive() && f.post().siteOperation().isEmpty(), "Animal obstruction pauses without damage or writes");
            OutpostGrowth.setMode(f.player(), f.post(), OutpostGrowth.Mode.MANUAL); pig.discard(); drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
            h.assertTrue(f.post().siteOperation().isEmpty(), "Manual mode never auto-applies"); h.succeed();
        });
    }
    private static void future(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            var original = f.post().saveWithFullMetadata(h.getLevel().registryAccess()).copy();
            var missing = original.copy(); missing.remove("growth"); var old = load(h, f.post(), missing);
            h.assertTrue(old.growthSupported() && old.growthSettings().equals(OutpostGrowth.Settings.DEFAULT), "Missing growth loads Manual, no automatic adoption");
            var future = OutpostGrowth.Settings.DEFAULT.save(); future.putInt("version", 99); future.putString("unknown", "keep");
            for (Tag raw : new Tag[]{future, StringTag.valueOf("opaque"), new CompoundTag()}) {
                var tag = original.copy(); tag.put("growth", raw.copy()); var post = load(h, old, tag);
                h.assertTrue(!post.growthSupported() && OutpostGrowth.status(h.getLevel(), post) == OutpostGrowth.UNAVAILABLE, "Malformed/future policy disables growth only");
                refused(h, () -> OutpostGrowth.setMode(h.getLevel(), post, OutpostGrowth.Mode.AUTOMATIC));
                refused(h, () -> OutpostExpansion.prepare(f.player(), post)); post.change(post.state().supplies(9)); drive(h, post, ResponseConfig.CHECK_INTERVAL.get());
                h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).get("growth").equals(raw) && post.state().supplies() == 9 && post.siteOperation().isEmpty(), "Raw policy retained while ordinary storage still works");
            }
            var known = original.copy(); known.getCompoundOrEmpty("growth").putString("extension", "keep"); var post = load(h, old, known);
            OutpostGrowth.setMode(h.getLevel(), post, OutpostGrowth.Mode.AUTOMATIC);
            var saved = post.saveWithFullMetadata(h.getLevel().registryAccess()); var reloaded = load(h, post, saved);
            h.assertTrue(reloaded.growthSettings().mode() == OutpostGrowth.Mode.AUTOMATIC && saved.getCompoundOrEmpty("growth").getStringOr("extension", "").equals("keep"), "Known policy reload and unknown fields retained"); h.succeed();
        });
    }
    private static BuildingBlockEntity load(GameTestHelper h, BuildingBlockEntity old, CompoundTag tag) {
        var post = (BuildingBlockEntity) BlockEntity.loadStatic(old.getBlockPos(), old.getBlockState(), tag, h.getLevel().registryAccess());
        h.assertTrue(post != null, "Growth data cannot crash building loading"); h.getLevel().setBlockEntity(post); return post;
    }
    private static void interrupted(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            OutpostGrowth.setMode(f.player(), f.post(), OutpostGrowth.Mode.AUTOMATIC);
            var inspection = OutpostPreview.inspectDetails(f.player(), f.post());
            f.post().storeSiteOperation(new ExpansionOperation(UUID.randomUUID(), f.post().site().id(), inspection.changes(), OutpostExpansion.protectedDigest(h.getLevel(), f.post(), f.post().site()), ExpansionOperation.Phase.PREPARED, 0, ""));
            drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
            h.assertTrue(f.post().growthSettings().pause() == OutpostGrowth.REVIEW && f.post().siteOperation().orElseThrow().observe(pos -> NbtUtils.writeBlockState(h.getLevel().getBlockState(pos))).state() == ExpansionOperation.Observed.ALL_BEFORE, "Even all-before interrupted intent pauses without replay");
            h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Menu cannot bypass interrupted receipt"); h.succeed();
        });
    }
    private static void commands(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            var dispatcher = h.getLevel().getServer().getCommands().getDispatcher(); var source = f.player().createCommandSourceStack();
            var op = source.withPermission(h.getLevel().getServer().createCommandSourceStack().permissions());
            try {
                for (String action : new String[]{"grow", "mode manual", "mode automatic"}) {
                    try { dispatcher.execute("standandhold outpost " + action, source); h.fail("Non-op cannot use admin " + action); }
                    catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { }
                }
                h.assertTrue(dispatcher.execute("standandhold outpost mode automatic", op) == 1 && f.post().growthSettings().mode() == OutpostGrowth.Mode.AUTOMATIC, "Real mode command shares saved policy");
                dispatcher.execute("standandhold outpost mode manual", op);
                h.assertTrue(dispatcher.execute("standandhold outpost help", op) == 1 && f.player().received("Advanced tools retained"), "Admin help documents both interfaces");
                var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("standandhold outpost mode ", op)).join().getList().stream().map(s -> s.getText()).toList();
                h.assertTrue(suggestions.contains("manual") && suggestions.contains("automatic"), "Mode literals provide argument completion");
                h.assertTrue(dispatcher.execute("standandhold outpost grow", op) == 1, "Operator immediate helper shares application"); applied(h, f.post());
                try { dispatcher.execute("standandhold outpost grow", op); h.fail("Repeated grow must refuse"); } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { }
                dispatcher.execute("standandhold outpost status", op);
                h.assertTrue(f.player().received("APPLIED / ALL_AFTER"), "Original diagnostics remain usable");
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) { throw new IllegalStateException(failure); }
            h.succeed();
        });
    }
    private static void gates(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            StructureConfig.EXPANSION.set(false);
            h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Disabled config blocks player action");
            h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.GROWTH_AUTOMATIC), "Disabled config blocks opt-in");
            h.assertTrue(f.post().siteOperation().isEmpty(), "No receipt when disabled"); StructureConfig.EXPANSION.set(true);
            var old = f.post().saveWithFullMetadata(h.getLevel().registryAccess()).copy(); old.remove("site"); var post = load(h, f.post(), old);
            h.assertTrue(OutpostGrowth.status(h.getLevel(), post) == OutpostGrowth.UNSUPPORTED, "Old/manual post is not adopted");
            refused(h, () -> OutpostGrowth.setMode(h.getLevel(), post, OutpostGrowth.Mode.AUTOMATIC));
            post.initializeMainBase(); h.assertTrue(OutpostGrowth.status(h.getLevel(), post) == OutpostGrowth.UNSUPPORTED, "Main Base unsupported");
            h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Replaced tile invalidates old menu"); h.succeed();
        });
    }
    private static void unload(GameTestHelper h) {
        QaWorld.begin(h); var level = h.getLevel(); var center = h.absolutePos(new BlockPos(1024, 2, 1024));
        int cx = center.getX() >> 4, cz = center.getZ() >> 4, y = center.getY();
        level.setChunkForced(cx, cz, true); level.getChunk(cx, cz);
        for (var pos : BlockPos.betweenClosed(cx << 4, y - 2, cz << 4, (cx << 4) + 15, y + 7, (cz << 4) + 15)) {
            level.setBlockAndUpdate(pos, (pos.getY() == y - 2 ? Blocks.DIRT : pos.getY() == y - 1 ? Blocks.GRASS_BLOCK : Blocks.AIR).defaultBlockState());
        }
        int radius = StructureConfig.CHECKPOINT_RADIUS.get(); StructurePlacement.Result placed;
        try { StructureConfig.CHECKPOINT_RADIUS.set(4); placed = StructurePlacement.place(level, StructureKind.CHECKPOINT, cx, cz); }
        finally { StructureConfig.CHECKPOINT_RADIUS.set(radius); }
        h.assertTrue(placed.success(), placed.message());
        var post = (BuildingBlockEntity) level.getBlockEntity(placed.commandPost()); post.change(new BuildingState(2, 13, 2, "", 0));
        OutpostGrowth.setMode(level, post, OutpostGrowth.Mode.AUTOMATIC); var id = post.site().id();
        StructureConfig.EXPANSION.set(false); level.setChunkForced(cx, cz, false); level.getChunkSource().save(true);
        awaitUnload(h, post, id, 0);
    }
    private static void newerGlobal(GameTestHelper h) {
        var f = begin(h, 2); h.runAfterDelay(3, () -> {
            var server = h.getLevel().getServer(); var original = HumanWorldData.get(server);
            OutpostGrowth.setMode(f.player(), f.post(), OutpostGrowth.Mode.AUTOMATIC);
            var future = (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow(); future.putInt("data_version", 99);
            server.getDataStorage().set(HumanWorldData.TYPE, HumanWorldData.CODEC.parse(NbtOps.INSTANCE, future).getOrThrow());
            try {
                drive(h, f.post(), ResponseConfig.CHECK_INTERVAL.get());
                h.assertTrue(OutpostGrowth.status(h.getLevel(), f.post()) == OutpostGrowth.UNAVAILABLE && f.post().siteOperation().isEmpty(), "Automatic growth cannot place against a newer global save");
                refused(h, () -> OutpostExpansion.prepare(f.player(), f.post()));
                h.assertFalse(f.menu().clickMenuButton(f.player(), BuildingMenu.EXPAND), "Menu respects unsupported global save");
                h.assertTrue(future.equals(HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, HumanWorldData.get(server)).getOrThrow()), "Unknown global save is retained without rewriting");
            } finally { server.getDataStorage().set(HumanWorldData.TYPE, original); }
            h.succeed();
        });
    }
    private static void awaitUnload(GameTestHelper h, BuildingBlockEntity old, UUID id, int attempt) {
        var level = h.getLevel();
        for (int i = 0; i < 350; i++) { level.getChunkSource().tick(() -> true, false); }
        if (level.hasChunkAt(old.getBlockPos())) {
            h.assertTrue(attempt < 25, "Real test chunk must unload after its forced/temporary tickets expire");
            h.runAfterDelay(10, () -> awaitUnload(h, old, id, attempt + 1)); return;
        }
        StructureConfig.EXPANSION.set(true); int chunks = level.getChunkSource().getLoadedChunksCount();
        OutpostGrowth.tick(level, old);
        h.assertTrue(!level.hasChunkAt(old.getBlockPos()) && level.getChunkSource().getLoadedChunksCount() == chunks, "Stale growth reference never reloads its chunk");
        StructureConfig.EXPANSION.set(false); level.getChunk(old.getBlockPos().getX() >> 4, old.getBlockPos().getZ() >> 4);
        var post = (BuildingBlockEntity) level.getBlockEntity(old.getBlockPos());
        h.assertTrue(post != null && post != old && post.site().id().equals(id) && post.growthSettings().mode() == OutpostGrowth.Mode.AUTOMATIC
                && post.siteOperation().isEmpty() && post.state().equals(new BuildingState(2, 13, 2, "", 0)), "Actual chunk serialization/unload/reload retains choice, identity and storage without on-load growth");
        StructureConfig.EXPANSION.set(true); drive(h, post, ResponseConfig.CHECK_INTERVAL.get()); applied(h, post);
        for (var pos : new BlockPos[]{post.getBlockPos(), post.getBlockPos().offset(-2, 0, -1), post.getBlockPos().offset(3, 0, -2)}) { level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()); }
        h.succeed();
    }
}
