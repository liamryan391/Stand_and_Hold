package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.threat.ThreatWorldData;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

final class PreviewTests {
    static Map<String, Consumer<GameTestHelper>> cases() {
        return Map.of("preview_read_only_command", PreviewTests::command, "preview_ineligible", PreviewTests::ineligible,
                "preview_site_save_safety", PreviewTests::saves, "preview_terrain_border", PreviewTests::terrain,
                "preview_entity_budget", PreviewTests::entities, "preview_unloaded", PreviewTests::unloaded,
                "preview_worldgen_boundary", PreviewTests::worldgen);
    }
    private record Fixture(BuildingBlockEntity post, QaPlayer player) { }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static Fixture begin(GameTestHelper h) {
        QaWorld.begin(h);
        var anchor = h.absolutePos(new BlockPos(32, 2, 32));
        int radius = StructureConfig.CHECKPOINT_RADIUS.get();
        StructurePlacement.Result result;
        try { StructureConfig.CHECKPOINT_RADIUS.set(4); result = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, anchor.getX() >> 4, anchor.getZ() >> 4); }
        finally { StructureConfig.CHECKPOINT_RADIUS.set(radius); }
        h.assertTrue(result.success(), "New checkpoint: " + result.message());
        var post = (BuildingBlockEntity) h.getLevel().getBlockEntity(result.commandPost());
        post.change(new BuildingState(2, 17, 2, "", 0));
        var player = QaPlayer.join(h.getLevel()); var pos = post.getBlockPos();
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16); player.setYHeadRot(180);
        return new Fixture(post, player);
    }
    private record Snapshot(Map<BlockPos, BlockState> blocks, Map<BlockPos, CompoundTag> tiles, CompoundTag human,
                            CompoundTag threat, int loadedChunks) { }
    private static Snapshot snapshot(GameTestHelper h, BuildingBlockEntity post) {
        var level = h.getLevel();
        data(h).setDirty(false); ThreatWorldData.get(level.getServer()).setDirty(false);
        level.getChunkAt(post.getBlockPos()).tryMarkSaved();
        var blocks = new LinkedHashMap<BlockPos, BlockState>(); var tiles = new LinkedHashMap<BlockPos, CompoundTag>();
        var center = post.getBlockPos().south().below();
        for (var cursor : BlockPos.betweenClosed(center.offset(-6, -1, -6), center.offset(6, 5, 6))) {
            var pos = cursor.immutable(); blocks.put(pos, level.getBlockState(pos));
            if (level.getBlockEntity(pos) instanceof BlockEntity tile) { tiles.put(pos, tile.saveWithFullMetadata(level.registryAccess())); }
        }
        return new Snapshot(blocks, tiles, (CompoundTag) HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, data(h)).getOrThrow(),
                (CompoundTag) ThreatWorldData.CODEC.encodeStart(NbtOps.INSTANCE, ThreatWorldData.get(level.getServer())).getOrThrow(), level.getChunkSource().getLoadedChunksCount());
    }
    private static void unchanged(GameTestHelper h, BuildingBlockEntity post, Snapshot before) {
        var level = h.getLevel();
        h.assertTrue(!data(h).isDirty() && !ThreatWorldData.get(level.getServer()).isDirty()
                && !level.getChunkAt(post.getBlockPos()).isUnsaved(), "Preview never dirties global/threat/chunk state");
        var after = snapshot(h, post);
        h.assertTrue(before.equals(after), "Blocks, complete building NBT, balances/rosters/registries/threat and loaded chunks unchanged");
    }
    private static void refused(GameTestHelper h, BuildingBlockEntity post, QaPlayer player, String reason) {
        var before = snapshot(h, post);
        try { OutpostPreview.inspect(player, post); h.fail("Expected refusal: " + reason); }
        catch (IllegalArgumentException | IllegalStateException failure) { h.assertTrue(failure.getMessage().contains(reason), "Specific refusal: " + failure.getMessage()); }
        unchanged(h, post, before);
    }
    private static void command(GameTestHelper h) {
        var f = begin(h);
        h.runAfterDelay(3, () -> {
            var before = snapshot(h, f.post()); var site = f.post().site();
            for (int i = 0; i < 3; i++) {
                var plan = OutpostPreview.inspect(f.player(), f.post());
                h.assertTrue(plan.cells().size() == 137 && plan.reads() == 616 && plan.columns() == 169, "Exact bounded read-only plan");
                h.assertTrue(f.post().site().equals(site), "Preview does not regenerate identity");
            }
            var server = h.getLevel().getServer(); var dispatcher = server.getCommands().getDispatcher();
            try {
                var source = f.player().createCommandSourceStack();
                try { dispatcher.execute("standandhold outpost preview", source); h.fail("Non-op command must refuse"); }
                catch (CommandSyntaxException expected) { }
                h.assertTrue(dispatcher.execute("standandhold outpost preview", source.withPermission(server.createCommandSourceStack().permissions())) == 1, "Real operator Brigadier path");
                h.assertTrue(f.player().received("READ ONLY") && f.player().received("88 ground") && f.player().received("claim-mod"), "Useful output/warning reached caller");
                h.assertTrue(dispatcher.getRoot().getChild("standandhold").getChild("outpost").getChild("expand") == null, "No expansion/confirm command");
            } catch (CommandSyntaxException failure) { throw new IllegalStateException(failure); }
            unchanged(h, f.post(), before); h.succeed();
        });
    }
    private static void ineligible(GameTestHelper h) {
        var f = begin(h);
        h.runAfterDelay(3, () -> {
            f.post().change(new BuildingState(1, 17, 2, "", 0)); refused(h, f.post(), f.player(), "local level 2");
            f.post().change(new BuildingState(2, 17, 2, "", 0));
            f.player().setGameMode(GameType.SPECTATOR); refused(h, f.post(), f.player(), "spectators"); f.player().setGameMode(GameType.SURVIVAL);
            var dimensions = StructureConfig.DIMENSIONS.get();
            try { StructureConfig.DIMENSIONS.set(List.of()); refused(h, f.post(), f.player(), "Dimension"); }
            finally { StructureConfig.DIMENSIONS.set(dimensions); }
            var original = f.post().saveWithFullMetadata(h.getLevel().registryAccess());
            var old = original.copy(); old.remove("site");
            var post = replace(h, f.post(), old); refused(h, post, f.player(), "No site provenance");
            for (int radius : new int[]{3, 5}) {
                var tag = original.copy(); tag.getCompoundOrEmpty("site").putInt("radius", radius);
                post = replace(h, post, tag); refused(h, post, f.player(), "radius/applied");
            }
            var mismatch = original.copy(); mismatch.getCompoundOrEmpty("site").putString("dimension", "minecraft:the_nether");
            post = replace(h, post, mismatch); refused(h, post, f.player(), "mismatch");
            post = replace(h, post, original); post.initializeMainBase(); refused(h, post, f.player(), "Main Base");
            var position = post.getBlockPos(); h.getLevel().setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            try { OutpostPreview.inspect(f.player(), post); h.fail("Removed post refused"); } catch (IllegalArgumentException expected) { }
            h.getLevel().setBlockAndUpdate(position, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
            var manual = (BuildingBlockEntity) h.getLevel().getBlockEntity(position); refused(h, manual, f.player(), "No site provenance");
            h.succeed();
        });
    }
    private static BuildingBlockEntity replace(GameTestHelper h, BuildingBlockEntity old, CompoundTag tag) {
        var level = h.getLevel();
        var loaded = (BuildingBlockEntity) BlockEntity.loadStatic(old.getBlockPos(), old.getBlockState(), tag, level.registryAccess());
        h.assertTrue(loaded != null, "Malformed/future site must not crash the ordinary building loader");
        level.setBlockEntity(loaded); return loaded;
    }
    private static void saves(GameTestHelper h) {
        var f = begin(h);
        h.runAfterDelay(3, () -> {
            var original = f.post().saveWithFullMetadata(h.getLevel().registryAccess());
            var extra = original.copy(); extra.getCompoundOrEmpty("site").putString("future_note", "keep");
            var post = replace(h, f.post(), extra); var before = snapshot(h, post);
            h.assertTrue(post.site().equals(f.post().site()), "UUID/layout/anchor/radius survives NBT loading");
            OutpostPreview.inspect(f.player(), post); unchanged(h, post, before);
            h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").getStringOr("future_note", "").equals("keep"), "Unknown site fields retained");
            for (Tag raw : new Tag[]{StringTag.valueOf("malformed but preserved"), extra.getCompoundOrEmpty("site").copy()}) {
                if (raw instanceof CompoundTag future) { future.putInt("site_version", 2); }
                var tag = original.copy(); tag.put("site", raw.copy()); post = replace(h, post, tag);
                refused(h, post, f.player(), raw instanceof CompoundTag ? "Unsupported" : "Malformed");
                post.change(post.state().supplies(19));
                h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).get("site").equals(raw), "Invalid/future data preserved during normal storage changes");
            }
            h.succeed();
        });
    }
    private static void terrain(GameTestHelper h) {
        var f = begin(h);
        h.runAfterDelay(3, () -> {
            var pos = f.post().site().center().offset(-6, 1, -6);
            h.getLevel().setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
            ((ChestBlockEntity) h.getLevel().getBlockEntity(pos)).setItem(0, net.minecraft.world.item.Items.DIAMOND.getDefaultInstance());
            refused(h, f.post(), f.player(), "Protected block entity");
            h.assertTrue(((ChestBlockEntity) h.getLevel().getBlockEntity(pos)).getItem(0).is(net.minecraft.world.item.Items.DIAMOND), "Player container/item untouched");
            h.getLevel().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState()); refused(h, f.post(), f.player(), "Wet/fluid");
            h.getLevel().setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState()); refused(h, f.post(), f.player(), "Occupied");
            h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            var border = h.getLevel().getWorldBorder(); double size = border.getSize();
            try { border.setSize(10); refused(h, f.post(), f.player(), "missing/changed"); }
            finally { border.setSize(size); }
            h.succeed();
        });
    }
    private static void entities(GameTestHelper h) {
        var f = begin(h);
        h.runAfterDelay(3, () -> {
            var wall = f.post().site().center().offset(-6, 1, -6);
            var cow = EntityTypes.COW.create(h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            cow.snapTo(wall.getX() + 0.5, wall.getY(), wall.getZ() + 0.5, 0, 0); cow.setNoAi(true); h.getLevel().addFreshEntity(cow);
            refused(h, f.post(), f.player(), "Entity obstructs"); cow.discard();
            for (int i = 0; i < 128; i++) {
                var pig = EntityTypes.PIG.create(h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                var pos = f.post().getBlockPos().south(2); pig.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0); pig.setNoAi(true); h.getLevel().addFreshEntity(pig);
            }
            refused(h, f.post(), f.player(), "inspection limit"); h.succeed();
        });
    }
    private static void unloaded(GameTestHelper h) {
        var f = begin(h);
        h.runAfterDelay(3, () -> {
            var pos = new BlockPos(29000008, 65, 29000007);
            h.assertTrue(!h.getLevel().hasChunkAt(pos), "Remote fixture chunk really is unloaded");
            var detached = new BuildingBlockEntity(pos, BuildingRegistry.COMMAND_POST.get().defaultBlockState()); detached.setLevel(h.getLevel());
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
            try { OutpostPreview.inspect(f.player(), detached); h.fail("Unloaded tile refused"); } catch (IllegalArgumentException expected) { }
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "No remote chunk access"); h.succeed();
        });
    }
    private static void worldgen(GameTestHelper h) {
        QaWorld.begin(h);
        var level = h.getLevel(); var anchor = h.absolutePos(new BlockPos(32, 2, 32));
        // Exercise the WorldGenLevel branch on the server thread, without claiming worker concurrency proof.
        var region = (WorldGenLevel) java.lang.reflect.Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class<?>[]{WorldGenLevel.class},
                (proxy, method, arguments) -> method.invoke(level, arguments));
        var result = StructurePlacement.place(region, StructureKind.CHECKPOINT, anchor.getX() >> 4, anchor.getZ() >> 4);
        h.assertTrue(result.success(), "Worldgen-style placement: " + result.message());
        var post = (BuildingBlockEntity) level.getBlockEntity(result.commandPost());
        h.assertTrue(post.site().origin().equals("worldgen") && data(h).commandPostKeys().isEmpty(), "Interface path initializes tile provenance without touching global registry");
        h.runAfterDelay(5, () -> { h.assertTrue(data(h).commandPostKeys().size() == 1, "Server onLoad performs deferred registration"); h.succeed(); });
    }
}
