package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.threat.ThreatWorldData;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StructureConfig;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Real writes and receipt persistence; injected adapters never enter the production jar. */
final class ExpansionTests {
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("expansion_once_two_players", ExpansionTests::once);
        tests.put("expansion_fail_first", h -> fault(h, 0));
        tests.put("expansion_fail_middle", h -> fault(h, 68));
        tests.put("expansion_fail_last", h -> fault(h, 136));
        tests.put("expansion_callback_container", h -> callback(h, false));
        tests.put("expansion_callback_late", h -> callback(h, true));
        tests.put("expansion_protected_callback", ExpansionTests::protectedCallback);
        tests.put("expansion_consent_changes", ExpansionTests::changes);
        tests.put("expansion_token_expires", ExpansionTests::expires);
        tests.put("expansion_receipt_recovery", ExpansionTests::recovery);
        tests.put("expansion_future_receipt", ExpansionTests::future);
        tests.put("expansion_operator_commands", ExpansionTests::commands);
        tests.put("expansion_changed_entity", ExpansionTests::entity);
        tests.put("expansion_original_container", ExpansionTests::originalContainer);
        tests.put("expansion_receipt_callback", ExpansionTests::receiptCallback);
        var scoped = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.forEach((name, test) -> scoped.put(name, h -> {
            boolean enabled = StructureConfig.EXPANSION.get(); StructureConfig.EXPANSION.set(true);
            h.testInfo.addListener(new GameTestListener() {
                public void testStructureLoaded(GameTestInfo info) { }
                public void testPassed(GameTestInfo info, GameTestRunner runner) { StructureConfig.EXPANSION.set(enabled); }
                public void testFailed(GameTestInfo info, GameTestRunner runner) { StructureConfig.EXPANSION.set(enabled); }
                public void testAddedForRerun(GameTestInfo old, GameTestInfo copy, GameTestRunner runner) { }
            }); test.accept(h);
        }));
        return scoped;
    }
    private record Fixture(BuildingBlockEntity post, QaPlayer player) { }
    private static Fixture begin(GameTestHelper h) {
        QaWorld.begin(h); int radius = StructureConfig.CHECKPOINT_RADIUS.get(); var center = h.absolutePos(new BlockPos(32, 2, 32));
        StructurePlacement.Result placed;
        try { StructureConfig.CHECKPOINT_RADIUS.set(4); placed = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, center.getX() >> 4, center.getZ() >> 4); }
        finally { StructureConfig.CHECKPOINT_RADIUS.set(radius); }
        h.assertTrue(placed.success(), placed.message());
        var post = (BuildingBlockEntity) h.getLevel().getBlockEntity(placed.commandPost()); post.change(new BuildingState(2, 17, 2, "", 0));
        var player = QaPlayer.join(h.getLevel()); aim(player, post); return new Fixture(post, player);
    }
    private static void aim(QaPlayer player, BuildingBlockEntity post) { var pos = post.getBlockPos(); player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 180, 16); player.setYHeadRot(180); }
    private record Snapshot(String protectedDigest, Tag human, Tag threat, int chunks) { }
    private static Snapshot snapshot(GameTestHelper h, BuildingBlockEntity post) {
        var level = h.getLevel(); return new Snapshot(OutpostExpansion.protectedDigest(level, post, post.site()),
                HumanWorldData.CODEC.encodeStart(NbtOps.INSTANCE, HumanWorldData.get(level.getServer())).getOrThrow(),
                ThreatWorldData.CODEC.encodeStart(NbtOps.INSTANCE, ThreatWorldData.get(level.getServer())).getOrThrow(), level.getChunkSource().getLoadedChunksCount());
    }
    private static void unchanged(GameTestHelper h, BuildingBlockEntity post, Snapshot before) { h.assertTrue(snapshot(h, post).equals(before), "Protected blocks/building NBT, global/threat/roster/cooldown state and loaded chunks unchanged"); }
    private static ExpansionOperation intent(Fixture f) {
        var inspection = OutpostPreview.inspectDetails(f.player(), f.post());
        return new ExpansionOperation(UUID.randomUUID(), f.post().site().id(), inspection.changes(),
                OutpostExpansion.protectedDigest(f.player().level(), f.post(), f.post().site()), ExpansionOperation.Phase.PREPARED, 0, "");
    }
    private static void refusal(GameTestHelper h, Runnable action, String fragment) {
        try { action.run(); h.fail("Expected safe refusal: " + fragment); }
        catch (IllegalArgumentException | IllegalStateException expected) { h.assertTrue(expected.getMessage().contains(fragment), expected.getMessage()); }
    }
    private static void once(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var before = snapshot(h, f.post()); var a = OutpostExpansion.prepare(f.player(), f.post());
            h.assertTrue(f.post().siteOperation().isEmpty(), "Preparing stores no world receipt or block writes");
            var other = QaPlayer.join(h.getLevel()); aim(other, f.post()); var b = OutpostExpansion.prepare(other, f.post());
            refusal(h, () -> OutpostExpansion.confirm(other, f.post(), a), "foreign");
            h.assertTrue(OutpostExpansion.confirm(f.player(), f.post(), a).success(), "First operator intent applies once");
            var operation = f.post().siteOperation().orElseThrow();
            h.assertTrue(operation.phase() == ExpansionOperation.Phase.APPLIED && f.post().site().appliedRevision() == 1
                    && operation.observe(pos -> NbtUtils.writeBlockState(h.getLevel().getBlockState(pos))).state() == ExpansionOperation.Observed.ALL_AFTER, "All 137 actual blocks and applied receipt verified");
            unchanged(h, f.post(), before);
            refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), a), "token");
            refusal(h, () -> OutpostExpansion.confirm(other, f.post(), b), "applied revision");
            var broken = operation.changes().getFirst().pos(); h.getLevel().setBlockAndUpdate(broken, Blocks.AIR.defaultBlockState());
            refusal(h, () -> OutpostExpansion.recover(f.player(), f.post()), "Already sealed");
            h.assertTrue(h.getLevel().getBlockState(broken).isAir(), "Repeat/recovery never repairs a broken applied apron");
            h.succeed();
        });
    }
    private static ExpansionApply.Access wrap(ExpansionApply.Access delegate, java.util.function.Predicate<ExpansionOperation.Change> write) {
        return new ExpansionApply.Access() {
            public boolean valid() { return delegate.valid(); }
            public CompoundTag read(BlockPos pos) { return delegate.read(pos); }
            public boolean write(ExpansionOperation.Change cell) { return write.test(cell); }
            public String protectedDigest() { return delegate.protectedDigest(); }
            public void store(ExpansionOperation receipt) { delegate.store(receipt); }
        };
    }
    private static void fault(GameTestHelper h, int stop) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var before = snapshot(h, f.post()); var operation = intent(f); var access = OutpostExpansion.access(f.player(), f.post(), operation); int[] index = {0};
            var result = ExpansionApply.apply(operation, wrap(access, cell -> {
                if (index[0]++ == stop) { if (stop == 68) { throw new IllegalStateException("Injected block callback failure"); } return false; }
                return access.write(cell);
            }));
            h.assertFalse(result.success(), "Required write fault must fail application");
            var saved = f.post().siteOperation().orElseThrow();
            h.assertTrue(saved.phase() == ExpansionOperation.Phase.REVIEW && saved.index() == stop && f.post().site().appliedRevision() == 0, "Bounded review receipt records first/middle/last failure");
            for (int i = 0; i < 137; i++) { var cell = operation.changes().get(i); h.assertTrue(NbtUtils.writeBlockState(h.getLevel().getBlockState(cell.pos())).equals(i < stop ? cell.after() : cell.before()), "No rollback or writes after failure at " + i); }
            unchanged(h, f.post(), before);
            refusal(h, () -> OutpostExpansion.prepare(f.player(), f.post()), "review");
            refusal(h, () -> OutpostExpansion.recover(f.player(), f.post()), "not fully matching"); h.succeed();
        });
    }
    private static void callback(GameTestHelper h, boolean late) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var before = snapshot(h, f.post()); var operation = intent(f); var access = OutpostExpansion.access(f.player(), f.post(), operation); int[] index = {0};
            var intruder = operation.changes().get(late ? 0 : 1).pos();
            var result = ExpansionApply.apply(operation, wrap(access, cell -> {
                boolean wrote = access.write(cell);
                if (index[0]++ == (late ? 136 : 0)) {
                    h.getLevel().setBlockAndUpdate(intruder, Blocks.CHEST.defaultBlockState());
                    ((ChestBlockEntity) h.getLevel().getBlockEntity(intruder)).setItem(0, net.minecraft.world.item.Items.DIAMOND.getDefaultInstance());
                }
                return wrote;
            }));
            h.assertFalse(result.success(), "Callback conflict cannot seal success");
            h.assertTrue(f.post().siteOperation().orElseThrow().phase() == ExpansionOperation.Phase.REVIEW
                    && ((ChestBlockEntity) h.getLevel().getBlockEntity(intruder)).getItem(0).is(net.minecraft.world.item.Items.DIAMOND), "New container/item survives without rollback/overwrite");
            unchanged(h, f.post(), before); refusal(h, () -> OutpostExpansion.recover(f.player(), f.post()), "not fully matching"); h.succeed();
        });
    }
    private static void protectedCallback(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var operation = intent(f); var access = OutpostExpansion.access(f.player(), f.post(), operation); int[] index = {0};
            var lab = (BuildingBlockEntity) h.getLevel().getBlockEntity(f.post().getBlockPos().offset(-2, 0, -1));
            h.assertFalse(ExpansionApply.apply(operation, wrap(access, cell -> {
                boolean wrote = access.write(cell); if (index[0]++ == 0) { lab.change(lab.state().supplies(99)); } return wrote;
            })).success(), "Protected-state callback change cannot be sealed");
            h.assertTrue(lab.state().supplies() == 99 && f.post().site().appliedRevision() == 0, "No destructive rollback of callback state");
            refusal(h, () -> OutpostExpansion.recover(f.player(), f.post()), "not fully matching"); h.succeed();
        });
    }
    private static void changes(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            StructureConfig.EXPANSION.set(false); refusal(h, () -> OutpostExpansion.prepare(f.player(), f.post()), "disabled"); StructureConfig.EXPANSION.set(true);
            var token = OutpostExpansion.prepare(f.player(), f.post());
            com.liamryan.standandhold.common.progression.HumanPointManager.setStage(h.getLevel().getServer(), com.liamryan.standandhold.common.progression.HumanStage.LOCAL_ARMY_RESPONSE);
            refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token), "stale");
            com.liamryan.standandhold.common.progression.HumanPointManager.setStage(h.getLevel().getServer(), com.liamryan.standandhold.common.progression.HumanStage.SURVIVORS);
            var token2 = OutpostExpansion.prepare(f.player(), f.post()); f.post().change(new BuildingState(3, 17, 2, "", 0));
            refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token2), "stale"); f.post().change(new BuildingState(2, 17, 2, "", 0));
            var token3 = OutpostExpansion.prepare(f.player(), f.post()); int slope = StructureConfig.SLOPE.get();
            try { StructureConfig.SLOPE.set(slope == 0 ? 1 : 0); refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token3), "stale"); } finally { StructureConfig.SLOPE.set(slope); }
            var token4 = OutpostExpansion.prepare(f.player(), f.post()); var pos = f.post().getBlockPos(); f.player().snapTo(pos.getX() + 30, pos.getY(), pos.getZ(), 0, 0);
            refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token4), "within 6"); aim(f.player(), f.post());
            var token5 = OutpostExpansion.prepare(f.player(), f.post()); var intruder = ExpansionPlan.layout(f.post().site()).getFirst().pos(); h.getLevel().setBlockAndUpdate(intruder.above(), Blocks.WATER.defaultBlockState());
            refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token5), "Wet/fluid");
            h.assertTrue(f.post().siteOperation().isEmpty(), "All changed-context refusals occur before intent persistence or application"); h.succeed();
        });
    }
    private static void expires(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var token = OutpostExpansion.prepare(f.player(), f.post());
            h.runAfterDelay(200, () -> { refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token), "expired"); h.assertTrue(f.post().siteOperation().isEmpty(), "Expired consent never saves intent"); h.succeed(); });
        });
    }
    private static void recovery(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var operation = intent(f); f.post().storeSiteOperation(operation); var before = snapshot(h, f.post());
            h.assertTrue(OutpostExpansion.status(f.player(), f.post()).contains("ALL_BEFORE"), "Prepared receipt only observes, never replays"); unchanged(h, f.post(), before);
            var access = OutpostExpansion.access(f.player(), f.post(), operation);
            for (var cell : operation.changes()) { h.assertTrue(access.write(cell), "Construct all-after recovery fixture"); }
            h.assertTrue(f.post().siteOperation().orElseThrow().phase() == ExpansionOperation.Phase.PREPARED, "Receipt deliberately not sealed by fixture");
            var blocks = operation.changes().stream().map(cell -> h.getLevel().getBlockState(cell.pos())).toList();
            h.assertTrue(OutpostExpansion.recover(f.player(), f.post()).contains("metadata only") && f.post().site().appliedRevision() == 1, "Explicit all-after inspection seals metadata once");
            h.assertTrue(blocks.equals(operation.changes().stream().map(cell -> h.getLevel().getBlockState(cell.pos())).toList()), "Recovery performs no block writes"); unchanged(h, f.post(), before); h.succeed();
        });
    }
    private static void future(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var operation = intent(f); f.post().storeSiteOperation(operation);
            var original = f.post().saveWithFullMetadata(h.getLevel().registryAccess()).copy();
            var known = original.copy(); var annotated = known.getCompoundOrEmpty("site").getCompoundOrEmpty("operation"); annotated.putString("unknown", "keep"); annotated.getListOrEmpty("cells").getCompoundOrEmpty(0).putString("unknown_cell", "keep");
            var loaded = replace(h, f.post(), known); loaded.storeSiteOperation(operation.withPhase(ExpansionOperation.Phase.REVIEW, 0, "Review fixture"));
            var saved = loaded.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").getCompoundOrEmpty("operation");
            h.assertTrue(loaded.siteOperation().orElseThrow().phase() == ExpansionOperation.Phase.REVIEW && saved.getStringOr("unknown", "").equals("keep")
                    && saved.getListOrEmpty("cells").getCompoundOrEmpty(0).getStringOr("unknown_cell", "").equals("keep"), "Known receipt NBT load/status transition retains unknown operation and cell fields");
            var future = operation.save(); future.putInt("operation_version", 2); future.putString("unknown", "keep");
            for (Tag raw : new Tag[]{future, StringTag.valueOf("malformed but preserved")}) {
                var tag = original.copy(); tag.getCompoundOrEmpty("site").put("operation", raw.copy()); var tile = replace(h, f.post(), tag);
                refusal(h, () -> OutpostExpansion.prepare(f.player(), tile), "Unsupported"); tile.change(tile.state().supplies(25));
                h.assertTrue(tile.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").get("operation").equals(raw), "Unsupported receipt retained exactly during ordinary storage changes");
            }
            var large = original.copy(); large.getCompoundOrEmpty("site").remove("operation"); large.getCompoundOrEmpty("site").putString("large_unknown", "x".repeat(300000)); var tile = replace(h, f.post(), large);
            refusal(h, () -> OutpostExpansion.prepare(f.player(), tile), "256 KiB");
            h.assertTrue(tile.saveWithFullMetadata(h.getLevel().registryAccess()).getCompoundOrEmpty("site").getStringOr("large_unknown", "").length() == 300000, "Oversized metadata refused without deletion or repair"); h.succeed();
        });
    }
    private static BuildingBlockEntity replace(GameTestHelper h, BuildingBlockEntity old, CompoundTag tag) {
        var loaded = (BuildingBlockEntity) BlockEntity.loadStatic(old.getBlockPos(), old.getBlockState(), tag, h.getLevel().registryAccess());
        h.assertTrue(loaded != null, "Unsupported operation does not crash ordinary building loading"); h.getLevel().setBlockEntity(loaded); return loaded;
    }
    private static void commands(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var dispatcher = h.getLevel().getServer().getCommands().getDispatcher();
            var source = f.player().createCommandSourceStack(); var operator = source.withPermission(h.getLevel().getServer().createCommandSourceStack().permissions());
            try {
                for (String action : new String[]{"prepare", "expand " + UUID.randomUUID(), "recover", "status"}) {
                    try { dispatcher.execute("standandhold outpost " + action, source); h.fail("Non-op must not use " + action); }
                    catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { }
                }
                dispatcher.execute("standandhold outpost prepare", operator);
                h.assertTrue(f.post().siteOperation().isEmpty() && f.player().received("no writes yet") && f.player().received("Player terrain"), "Preparing gives consent text without changing world");
                String command = f.player().suggestedCommand("/standandhold outpost expand ");
                h.assertTrue(dispatcher.execute(command.substring(1), operator) == 1, "Actual suggested Brigadier command applies safely");
                dispatcher.execute("standandhold outpost status", operator);
                h.assertTrue(f.player().received("APPLIED / ALL_AFTER") && f.player().received("Apron applied once"), "Readable once-only operation status reaches operator");
                try { dispatcher.execute("standandhold outpost expand not-a-uuid", operator); h.fail("Malformed token must refuse"); } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) { }
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) { throw new IllegalStateException(failure); }
            h.succeed();
        });
    }
    private static void entity(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var token = OutpostExpansion.prepare(f.player(), f.post()); var before = snapshot(h, f.post());
            var pos = f.post().site().center().offset(-6, 1, -6);
            var pig = net.minecraft.world.entity.EntityTypes.PIG.create(h.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            pig.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0); pig.setNoAi(true); h.getLevel().addFreshEntity(pig);
            refusal(h, () -> OutpostExpansion.confirm(f.player(), f.post(), token), "Entity obstructs");
            h.assertTrue(f.post().siteOperation().isEmpty(), "Late entity obstruction rejected before any receipt/block write"); unchanged(h, f.post(), before); h.succeed();
        });
    }
    private static void originalContainer(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var pos = f.post().getBlockPos().south(2); h.getLevel().setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
            ((ChestBlockEntity) h.getLevel().getBlockEntity(pos)).setItem(0, net.minecraft.world.item.Items.DIAMOND.getDefaultInstance());
            refusal(h, () -> OutpostExpansion.prepare(f.player(), f.post()), "Extra protected block entity");
            h.assertTrue(f.post().siteOperation().isEmpty() && ((ChestBlockEntity) h.getLevel().getBlockEntity(pos)).getItem(0).is(net.minecraft.world.item.Items.DIAMOND), "Extra player container is untouched and never hashed without a bound"); h.succeed();
        });
    }
    private static void receiptCallback(GameTestHelper h) {
        var f = begin(h); h.runAfterDelay(3, () -> {
            var before = snapshot(h, f.post()); var operation = intent(f); var access = OutpostExpansion.access(f.player(), f.post(), operation); int[] writes = {0};
            var result = ExpansionApply.apply(operation, wrap(access, cell -> {
                boolean wrote = access.write(cell);
                if (++writes[0] == 1) { f.post().storeSiteOperation(operation.withPhase(ExpansionOperation.Phase.REVIEW, 0, "External callback changed intent status")); }
                return wrote;
            }));
            h.assertFalse(result.success(), "Receipt mutation cannot be silently sealed/applied");
            h.assertTrue(writes[0] == 1 && f.post().siteOperation().orElseThrow().phase() == ExpansionOperation.Phase.REVIEW
                    && f.post().siteOperation().orElseThrow().index() == 1, "Transient metadata epoch stops before the next block write");
            unchanged(h, f.post(), before); h.succeed();
        });
    }
}
