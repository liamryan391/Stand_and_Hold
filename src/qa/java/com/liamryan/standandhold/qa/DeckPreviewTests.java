package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.threat.ThreatWorldData;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;

final class DeckPreviewTests {
    private record Fixture(BuildingBlockEntity post, QaPlayer player) { }
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("deck_preview_direct_readonly", h -> supported(h, false));
        tests.put("deck_preview_apron_readonly", h -> supported(h, true));
        tests.put("deck_preview_every_roof_and_write", DeckPreviewTests::edits);
        tests.put("deck_preview_protected_envelope", DeckPreviewTests::terrain);
        tests.put("deck_preview_entity_obstruction", DeckPreviewTests::entities);
        tests.put("deck_preview_unknown_receipts", DeckPreviewTests::receipts);
        tests.put("deck_preview_level_and_actor", DeckPreviewTests::eligibility);
        tests.put("deck_preview_command_menu_authority", DeckPreviewTests::commands);
        tests.put("deck_preview_no_automatic_application", DeckPreviewTests::automatic);
        tests.put("deck_preview_unloaded", DeckPreviewTests::unloaded);
        var scoped = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.forEach((name, test) -> scoped.put(name, h -> {
            boolean expansion = StructureConfig.EXPANSION.get(), deploy = NpcConfig.DEPLOY.get(), raids = ResponseConfig.RAIDS.get(), reinforcements = ResponseConfig.REINFORCEMENTS.get();
            StructureConfig.EXPANSION.set(true); NpcConfig.DEPLOY.set(false); ResponseConfig.RAIDS.set(false); ResponseConfig.REINFORCEMENTS.set(false);
            h.testInfo.addListener(new GameTestListener() {
                private void restore() { StructureConfig.EXPANSION.set(expansion); NpcConfig.DEPLOY.set(deploy); ResponseConfig.RAIDS.set(raids); ResponseConfig.REINFORCEMENTS.set(reinforcements); }
                public void testStructureLoaded(GameTestInfo i) { }
                public void testPassed(GameTestInfo i, GameTestRunner r) { restore(); }
                public void testFailed(GameTestInfo i, GameTestRunner r) { restore(); }
                public void testAddedForRerun(GameTestInfo a, GameTestInfo b, GameTestRunner r) { }
            });
            test.accept(h);
        }));
        return scoped;
    }
    private static Fixture begin(GameTestHelper h, boolean apron) {
        QaWorld.begin(h); var p = h.absolutePos(new BlockPos(32, 2, 32)); int radius = StructureConfig.CHECKPOINT_RADIUS.get(); StructurePlacement.Result placed;
        try { StructureConfig.CHECKPOINT_RADIUS.set(4); placed = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, p.getX() >> 4, p.getZ() >> 4); }
        finally { StructureConfig.CHECKPOINT_RADIUS.set(radius); }
        h.assertTrue(placed.success(), placed.message()); var post = (BuildingBlockEntity) h.getLevel().getBlockEntity(placed.commandPost());
        post.change(new BuildingState(3, 71, 3, "", 0));
        ((BuildingBlockEntity) h.getLevel().getBlockEntity(post.getBlockPos().offset(-2, 0, -1))).change(new BuildingState(1, 29, 4, "standandhold:field_communications", 5, false));
        var player = QaPlayer.join(h.getLevel()); var pos = post.getBlockPos(); player.snapTo(pos.getX()+.5, pos.getY(), pos.getZ()+4.5, 180, 16); player.setYHeadRot(180);
        if (apron) { h.assertTrue(OutpostExpansion.applyNow(player, post).success(), "Real prior apron receipt"); }
        OutpostGrowth.expand(player, post);
        return new Fixture(post, player);
    }
    private record Snapshot(Map<BlockPos, BlockState> blocks, Map<BlockPos, CompoundTag> tiles, Tag human, Tag threat,
                            Map<UUID, CompoundTag> entities, int chunks) { }
    private static Snapshot snapshot(GameTestHelper h, BuildingBlockEntity post) {
        var level = h.getLevel(); var human = HumanWorldData.get(level.getServer()); var threat = ThreatWorldData.get(level.getServer());
        human.setDirty(false); threat.setDirty(false); level.getChunkAt(post.getBlockPos()).tryMarkSaved();
        var blocks = new LinkedHashMap<BlockPos, BlockState>(); var tiles = new LinkedHashMap<BlockPos, CompoundTag>(); var c = post.site().center();
        for (var cursor : BlockPos.betweenClosed(c.offset(-6, -1, -6), c.offset(6, 7, 6))) {
            var pos = cursor.immutable(); blocks.put(pos, level.getBlockState(pos));
            if (level.getBlockEntity(pos) instanceof BlockEntity tile) { tiles.put(pos, tile.saveWithFullMetadata(level.registryAccess())); }
        }
        var entities = new HashMap<UUID, CompoundTag>();
        for (var entity : level.getEntities((Entity) null, new net.minecraft.world.phys.AABB(c.getX()-6,c.getY(),c.getZ()-6,c.getX()+7,c.getY()+8,c.getZ()+7))) {
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess()); entity.saveWithoutId(output); entities.put(entity.getUUID(), output.buildResult());
        }
        return new Snapshot(blocks, tiles, HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, human).getOrThrow(),
                ThreatWorldData.CODEC.encodeStart(NbtOps.INSTANCE, threat).getOrThrow(), entities, level.getChunkSource().getLoadedChunksCount());
    }
    private static void unchanged(GameTestHelper h, BuildingBlockEntity post, Snapshot before) {
        h.assertTrue(!HumanWorldData.get(h.getLevel().getServer()).isDirty() && !ThreatWorldData.get(h.getLevel().getServer()).isDirty()
                && !h.getLevel().getChunkAt(post.getBlockPos()).isUnsaved(), "Preview leaves global/threat/chunk dirty flags clear");
        h.assertTrue(snapshot(h, post).equals(before), "Exact blocks, full tile/entity NBT, balances/rosters/cooldowns and chunks unchanged");
    }
    private static void refused(GameTestHelper h, BuildingBlockEntity post, Runnable action, String reason) {
        var before = snapshot(h, post);
        try { action.run(); h.fail("Expected read-only refusal: " + reason); }
        catch (IllegalArgumentException | IllegalStateException failure) { h.assertTrue(failure.getMessage().contains(reason), failure.getMessage()); }
        unchanged(h, post, before);
    }
    private static void supported(GameTestHelper h, boolean apron) {
        var f = begin(h, apron); h.runAfterDelay(3, () -> {
            var npc = h.spawn(NpcRegistry.SURVIVOR.get(), h.relativePos(f.post().site().center().offset(-2, 1, 3))); npc.setNoAi(true); npc.setHealth(9);
            var before = snapshot(h, f.post());
            for (int i = 0; i < 3; i++) {
                var plan = LevelThreePreview.inspect(f.player(), f.post());
                h.assertTrue(plan.changes().size() == 63 && plan.reads() == 1521, "Real revision-2 yard matches exact candidate/source states");
            }
            unchanged(h, f.post(), before); h.succeed();
        });
    }
    private static void edits(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            var level = h.getLevel(); var c = f.post().site().center();
            for (int z = -2; z <= 0; z++) { for (int x = -1; x <= 1; x++) {
                var pos = c.offset(x, 4, z); var old = level.getBlockState(pos); level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "source/headroom changed"); level.setBlockAndUpdate(pos, old);
            } }
            for (var cell : LevelThreePlan.layout(f.post().site())) {
                var old = level.getBlockState(cell.pos()); level.setBlockAndUpdate(cell.pos(), Blocks.CHEST.defaultBlockState());
                ((ChestBlockEntity) level.getBlockEntity(cell.pos())).setItem(0, net.minecraft.world.item.Items.DIAMOND.getDefaultInstance());
                refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "container/fluid");
                h.assertTrue(((ChestBlockEntity) level.getBlockEntity(cell.pos())).getItem(0).is(net.minecraft.world.item.Items.DIAMOND), "Player chest contents retained");
                level.setBlockAndUpdate(cell.pos(), old);
            }
            h.succeed();
        });
    }
    private static void terrain(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            var level = h.getLevel(); var c = f.post().site().center();
            for (var pos : List.of(c.offset(2,6,3), c.offset(0,6,0), c.offset(4,1,6), c.offset(-4,2,0), c.offset(0,-1,0), c.offset(0,0,0))) {
                var old = level.getBlockState(pos); level.setBlockAndUpdate(pos, Blocks.GOLD_BLOCK.defaultBlockState());
                refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), pos.getY() < c.getY() ? "natural support" : "source/headroom changed");
                level.setBlockAndUpdate(pos, old);
            }
            var wet = c.offset(2,6,3); level.setBlockAndUpdate(wet, Blocks.WATER.defaultBlockState());
            refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "container/fluid"); level.setBlockAndUpdate(wet, Blocks.AIR.defaultBlockState());
            var border = level.getWorldBorder(); double size = border.getSize(); double x = border.getCenterX(), z = border.getCenterZ();
            try { border.setCenter(c.getX(), c.getZ()); border.setSize(10); refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "border"); }
            finally { border.setCenter(x, z); border.setSize(size); }
            h.succeed();
        });
    }
    private static void entities(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            var c = f.post().site().center(); var cow = EntityTypes.COW.create(h.getLevel(), EntitySpawnReason.COMMAND); cow.setNoAi(true);
            for (var pos : List.of(c.offset(2,1,5), c.offset(0,5,-1))) {
                cow.snapTo(pos.getX()+.5, pos.getY(), pos.getZ()+.5, 0, 0);
                if (h.getLevel().getEntity(cow.getUUID()) == null) { h.getLevel().addFreshEntity(cow); }
                refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "minecraft:cow");
            }
            cow.discard();
            for (int i = 0; i < 128; i++) { var pig = EntityTypes.PIG.create(h.getLevel(), EntitySpawnReason.COMMAND); pig.setNoAi(true); pig.snapTo(c.getX()-1.5, c.getY()+1, c.getZ()+3.5, 0, 0); h.getLevel().addFreshEntity(pig); }
            refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "inspection limit"); h.succeed();
        });
    }
    private static BuildingBlockEntity load(GameTestHelper h, BuildingBlockEntity post, CompoundTag tag) {
        var replacement = (BuildingBlockEntity) BlockEntity.loadStatic(post.getBlockPos(), post.getBlockState(), tag, h.getLevel().registryAccess());
        h.assertTrue(replacement != null, "Unknown site extensions do not crash normal tile load"); h.getLevel().setBlockEntity(replacement); return replacement;
    }
    private static void receipts(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            var saved = f.post().saveWithFullMetadata(h.getLevel().registryAccess());
            for (String mode : List.of("missing", "future", "reordered", "mixed", "extra-apron")) {
                var tag = saved.copy(); var site = tag.getCompoundOrEmpty("site"); var yard = site.getCompoundOrEmpty("yard_operation");
                switch (mode) {
                    case "missing" -> site.remove("yard_operation");
                    case "future" -> yard.putInt("operation_version", 99);
                    case "reordered" -> { var cells = yard.getListOrEmpty("cells"); var first = cells.removeFirst(); cells.add(first); }
                    case "mixed" -> { yard.putString("phase", "REVIEW"); yard.putInt("index", 7); }
                    case "extra-apron" -> site.put("operation", StringTag.valueOf("unknown"));
                }
                var post = load(h, f.post(), tag);
                refused(h, post, () -> LevelThreePreview.inspect(h.getLevel(), post), "");
                h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).get("site").equals(site), "Unknown receipt bytes retained, not migrated");
            }
            var extra = saved.copy(); extra.getCompoundOrEmpty("site").putString("future_note", "keep");
            var post = load(h, f.post(), extra); post.change(post.state().supplies(909)); var before = snapshot(h, post);
            LevelThreePreview.inspect(h.getLevel(), post); unchanged(h, post, before); h.succeed();
        });
    }
    private static void eligibility(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            f.post().change(new BuildingState(2,71,3,"",0)); refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "local level 3");
            f.post().change(new BuildingState(5,71,3,"",0)); LevelThreePreview.inspect(f.player(), f.post());
            f.player().setGameMode(GameType.SPECTATOR); refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "spectators");
            f.player().setGameMode(GameType.SURVIVAL); var p = f.post().getBlockPos(); f.player().snapTo(p.getX()+15,p.getY(),p.getZ(),0,0);
            refused(h, f.post(), () -> LevelThreePreview.inspect(f.player(), f.post()), "6 blocks");
            f.post().initializeMainBase(); refused(h, f.post(), () -> LevelThreePreview.inspect(h.getLevel(), f.post()), "Main Base"); h.succeed();
        });
    }
    private static void commands(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            var server = h.getLevel().getServer(); var dispatcher = server.getCommands().getDispatcher(); var source = f.player().createCommandSourceStack(); var before = snapshot(h, f.post());
            try {
                try { dispatcher.execute("standandhold outpost layout", source); h.fail("Operator-only command"); } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { }
                h.assertTrue(dispatcher.execute("standandhold outpost layout", source.withPermission(server.createCommandSourceStack().permissions())) == 1, "Real layout dispatch selects deck at revision 2");
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) { throw new IllegalStateException(failure); }
            h.assertTrue(f.player().received("Level-3 upper deck / READ ONLY") && f.player().received("Placement is not enabled"), "Honest read-only output");
            var menu = new BuildingMenu(72, f.player().getInventory(), f.post(), FoundationResources.get(server)); f.player().containerMenu = menu;
            h.assertTrue(menu.number(BuildingMenu.LAYOUT_REVISION) == 2 && menu.clickMenuButton(f.player(), BuildingMenu.PREVIEW_DECK), "Ordinary local non-op can preview via authoritative menu");
            unchanged(h, f.post(), before);
            var other = QaPlayer.join(h.getLevel()); other.containerMenu = menu;
            before = snapshot(h, f.post());
            h.assertFalse(menu.clickMenuButton(other, BuildingMenu.PREVIEW_DECK), "Other player's menu rejected");
            unchanged(h, f.post(), before);
            f.player().setGameMode(GameType.SPECTATOR); before = snapshot(h, f.post());
            h.assertFalse(menu.clickMenuButton(f.player(), BuildingMenu.PREVIEW_DECK), "Spectator menu rejected");
            unchanged(h, f.post(), before); h.succeed();
        });
    }
    private static void automatic(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            OutpostGrowth.setMode(h.getLevel(), f.post(), OutpostGrowth.Mode.AUTOMATIC); var before = snapshot(h, f.post());
            for (int i = 0; i < 10; i++) { OutpostGrowth.tick(h.getLevel(), f.post()); LevelThreePreview.inspect(h.getLevel(), f.post()); }
            refused(h, f.post(), () -> OutpostGrowth.expand(f.player(), f.post()), "Already expanded"); unchanged(h, f.post(), before);
            boolean enabled = StructureConfig.EXPANSION.get();
            try { StructureConfig.EXPANSION.set(false); LevelThreePreview.inspect(h.getLevel(), f.post()); unchanged(h, f.post(), before); }
            finally { StructureConfig.EXPANSION.set(enabled); }
            h.succeed();
        });
    }
    private static void unloaded(GameTestHelper h) {
        var f = begin(h, false); h.runAfterDelay(3, () -> {
            var pos = new BlockPos(29000008,65,29000007); h.assertFalse(h.getLevel().hasChunkAt(pos), "Remote fixture actually unloaded");
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount(); var post = new BuildingBlockEntity(pos, BuildingRegistry.COMMAND_POST.get().defaultBlockState()); post.setLevel(h.getLevel());
            try { LevelThreePreview.inspect(h.getLevel(), post); h.fail("Detached tile refused"); } catch (IllegalArgumentException expected) { }
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "Preview never requests unloaded chunks"); h.succeed();
        });
    }
}
