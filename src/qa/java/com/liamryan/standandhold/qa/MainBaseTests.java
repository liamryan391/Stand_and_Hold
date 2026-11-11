package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

final class MainBaseTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("mainbase_compact_zones", MainBaseTests::zones);
        tests.put("mainbase_protect_custom_block", h -> refused(h, Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.RED), false));
        tests.put("mainbase_refuse_fluid", h -> refused(h, Blocks.WATER, false));
        tests.put("mainbase_refuse_steep", h -> refused(h, Blocks.GRASS_BLOCK, true));
        tests.put("mainbase_site_activation_duties", MainBaseTests::activation);
        tests.put("mainbase_loaded_status_cleanup", MainBaseTests::commands);
        tests.put("mainbase_optional_layout_data", MainBaseTests::layoutData);
        return tests;
    }
    private static StructurePlacement.Result place(GameTestHelper h) {
        var p = h.absolutePos(HOME);
        return StructurePlacement.place(h.getLevel(), StructureKind.MAIN_BASE, p.getX() >> 4, p.getZ() >> 4);
    }
    private static BuildingBlockEntity tile(GameTestHelper h, BlockPos pos) {
        return (BuildingBlockEntity) h.getLevel().getBlockEntity(pos);
    }
    private static BuildingBlockEntity base(GameTestHelper h) {
        var result = place(h);
        h.assertTrue(result.success(), "Actual new base placed: " + result.message());
        return tile(h, result.commandPost());
    }
    private static void zones(GameTestHelper h) {
        QaWorld.begin(h);
        int before = StructureConfig.MAIN_BASE_RADIUS.get();
        StructureConfig.MAIN_BASE_RADIUS.set(5);
        try {
            var post = base(h); var p = post.getBlockPos();
            var lab = tile(h, p.offset(-3, 0, -2)); var crate = tile(h, p.offset(4, 0, -3));
            h.assertTrue(post.mainBaseLayoutLabel().equals("inhabited layout v1") && post.state().equals(BuildingState.EMPTY), "New provenance and unchanged post resources");
            h.assertTrue(lab.kind() == BuildingKind.LAB && lab.state().equals(BuildingState.EMPTY) && crate.kind() == BuildingKind.CRATE
                    && crate.state().supplies() == StructureConfig.MAIN_BASE_SUPPLIES.get() && crate.state().samples() == 0, "Only existing once-only crate supplies");
            var level = h.getLevel();
            h.assertTrue(level.getBlockState(p.south(6).above(3)).is(Blocks.POLISHED_DEEPSLATE_SLAB)
                    && level.getBlockState(p.offset(-3, 0, 4)).is(Blocks.SMOOTH_STONE_SLAB)
                    && level.getBlockState(p.south(6)).isAir(), "Gate, barracks bench and walking clearance are real blocks");
            h.assertTrue(TierTests.data(h).commandPostKeys().size() == 1 && TierTests.data(h).mainBaseKeys().size() == 1
                    && !TierTests.data(h).hasDiscoveredMainBase(), "Single correct anchor; placement itself is not discovery");
            h.assertFalse(place(h).success(), "Repeat placement refuses occupied structure");
            h.assertTrue(crate.state().supplies() == StructureConfig.MAIN_BASE_SUPPLIES.get(), "No extra crate stock");
        } finally { StructureConfig.MAIN_BASE_RADIUS.set(before); }
        h.runAfterDelay(5, h::succeed);
    }
    private static void refused(GameTestHelper h, net.minecraft.world.level.block.Block block, boolean steep) {
        QaWorld.begin(h);
        var level = h.getLevel(); var p = h.absolutePos(HOME);
        var center = new BlockPos((p.getX() & ~15) + 8, p.getY(), (p.getZ() & ~15) + 8);
        var obstruction = center.offset(-3, steep ? 3 : 0, -2);
        if (steep) { for (int i = 1; i <= 3; i++) { level.setBlockAndUpdate(obstruction.below(i), Blocks.DIRT.defaultBlockState()); } }
        level.setBlockAndUpdate(obstruction, block.defaultBlockState());
        var result = place(h);
        h.assertFalse(result.success(), "Refuse " + block + " terrain: " + result.message());
        h.assertTrue(level.getBlockState(obstruction).is(block) && level.getBlockState(center.below()).is(Blocks.GRASS_BLOCK)
                && level.getBlockState(center.north()).isAir(), "No custom-block, ground or anchor changes on refusal");
        h.assertTrue(TierTests.data(h).mainBaseKeys().isEmpty() && TierTests.data(h).commandPostKeys().isEmpty(), "No partial registration");
        h.succeed();
    }
    private static void activation(GameTestHelper h) {
        QaWorld.begin(h); var post = base(h); var level = h.getLevel(); var data = TierTests.data(h);
        var actor = QaPlayer.join(level); var p = post.getBlockPos();
        actor.snapTo(p.getX()+.5, p.getY(), p.getZ()+2.5, 180, 10);
        var catalog = FoundationResources.get(level.getServer());
        var home = new BuildingPosition(level.dimension().identifier(), p);
        h.runAfterDelay(3, () -> {
            h.useBlock(h.relativePos(p), actor); h.useBlock(h.relativePos(p), actor);
            var menu = new BuildingMenu(1, actor.getInventory(), post, catalog);
            h.assertTrue(actor.messages("message.standandhold.main_base_dormant") == 1 && data.hasDiscoveredMainBase(), "Dormant interaction discovers site and announces once");
            h.assertTrue(menu.number(BuildingMenu.GENERATION_POINTS) == 0 && menu.number(BuildingMenu.DEPLOYMENT_STATUS) == 2
                    && DefenderDeployment.attempt(level, post) == null, "Dormancy shown and enforced");
            var crate = tile(h, p.offset(StructureConfig.MAIN_BASE_RADIUS.get()-1, 0, 2-StructureConfig.MAIN_BASE_RADIUS.get()));
            var cp = crate.getBlockPos(); actor.snapTo(cp.getX()+.5, cp.getY(), cp.getZ()+1.5, 180, 10);
            var crateMenu = new BuildingMenu(2, actor.getInventory(), crate, catalog); actor.containerMenu = crateMenu;
            long stock = crate.state().supplies();
            long batch = com.liamryan.standandhold.config.BuildingConfig.TRANSFER.get();
            if (stock < batch) {
                h.assertFalse(crateMenu.clickMenuButton(actor, BuildingMenu.EXPORT), "Starter stock alone does not bypass the existing full-batch rule");
            }
            data.setSupplies(batch);
            h.assertTrue(crateMenu.clickMenuButton(actor, BuildingMenu.IMPORT) && data.supplies() == 0
                    && crate.state().supplies() == stock + batch
                    && crateMenu.clickMenuButton(actor, BuildingMenu.EXPORT) && data.supplies() == batch
                    && crate.state().supplies() == stock, "Dormancy leaves full-batch transfers usable without losing starter stock");
            actor.snapTo(p.getX()+.5, p.getY(), p.getZ()+2.5, 180, 10);
            TierTests.unlock(h); TierTests.stage(h, 5);
            var entry = catalog.research("special_division_training");
            var result = BuildingTransactions.finishResearch(new BuildingState(1, 100, 5, entry.id(), 12, true),
                    data, entry, HumanProgressionConfig.thresholds(), 12);
            h.assertTrue(result.supplies() == 0 && result.samples() == 0 && data.isResearchComplete(entry.id()), "Real training transaction required");
            h.useBlock(h.relativePos(p), actor); h.useBlock(h.relativePos(p), actor);
            h.assertTrue(actor.messages("message.standandhold.main_base_active") == 1 && post.operational(data), "Activation announces once");
            DefenderDuties.setMode(level, post, DefenderDuty.Mode.PATROL);
            for (int i = 0; i < 3; i++) {
                var npc = DefenderDeployment.attempt(level, post);
                h.assertTrue(npc != null && npc.tier() == (i == 0 ? HumanTier.SPECIAL_DIVISION : HumanTier.SUPER_ELITE), "Actual specialist/fallback in authored site " + i);
                h.assertTrue(DefenderDuties.post(level, npc) == post, "Shared assigned duty");
            }
            for (int i = 0; i < 2; i++) {
                var waypoint = DefenderDuties.waypoint(post, i);
                h.assertTrue(level.getBlockState(waypoint).isAir() && level.getBlockState(waypoint.above()).isAir()
                        && level.getBlockState(waypoint.below()).is(Blocks.POLISHED_ANDESITE), "Both real patrol waypoints on the open crosswalk");
            }
            h.assertTrue(data.hasDivisionDefender(home.key()) && data.defenderCount(home.key()) == 3
                    && DefenderDeployment.attempt(level, post) == null && post.state().level() == 1, "Shared cap; activation did not buy an upgrade");
            h.succeed();
        });
    }
    private static void commands(GameTestHelper h) {
        QaWorld.begin(h); var post = base(h); var p = post.getBlockPos(); var level = h.getLevel();
        var actor = QaPlayer.join(level);
        actor.snapTo(p.getX()+.5, p.getY(), p.getZ()+2.5, 180, 10);
        var data = TierTests.data(h);
        var distant = new BuildingPosition(level.dimension().identifier(), p.offset(16384, 0, 16384));
        data.registerMainBase(distant);
        h.runAfterDelay(3, () -> {
            var dispatcher = level.getServer().getCommands().getDispatcher();
            var source = actor.createCommandSourceStack().withPermission(level.getServer().createCommandSourceStack().permissions());
            try {
                h.assertTrue(!level.hasChunkAt(distant.pos()), "Distant registered chunk starts unloaded");
                h.assertTrue(dispatcher.execute("standandhold mainbase list", source) == 2 && actor.received("inhabited layout v1")
                        && actor.received("not inspected/unloaded"), "List labels new and uninspected sites");
                h.assertTrue(dispatcher.execute("standandhold mainbase nearest", source) == 1
                        && dispatcher.execute("standandhold mainbase status", source) == 1 && actor.received("[dormant]"), "Nearest/status show same loaded anchor");
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                var stale = new BuildingPosition(level.dimension().identifier(), p);
                data.registerMainBase(stale); // Simulate an external removal that missed the block hook.
                h.assertTrue(dispatcher.execute("standandhold mainbase list", source.withPosition(Vec3.atCenterOf(p))) == 1
                        && !data.isMainBase(stale) && !data.commandPostKeys().contains(stale.key())
                        && data.isMainBase(distant) && !level.hasChunkAt(distant.pos()), "Cleanup drops only the loaded missing anchor without loading anything");
            } catch (CommandSyntaxException failure) { throw new IllegalStateException(failure); }
            h.succeed();
        });
    }
    private static void layoutData(GameTestHelper h) {
        QaWorld.begin(h); var post = base(h);
        var registry = h.getLevel().registryAccess();
        var tag = post.saveWithFullMetadata(registry);
        var unknown = new CompoundTag(); unknown.putString("future", "keep");
        for (var value : java.util.List.of(net.minecraft.nbt.IntTag.valueOf(2), unknown)) {
            tag.put("main_base_layout", value);
            var loaded = (BuildingBlockEntity) BlockEntity.loadStatic(post.getBlockPos(), post.getBlockState(), tag, registry);
            h.assertTrue(loaded != null && loaded.isMainBase() && loaded.mainBaseLayoutLabel().equals("unknown layout (retained)")
                    && value.equals(loaded.saveWithFullMetadata(registry).get("main_base_layout")), "Unknown revision or malformed layout preserved");
            loaded.requireWritable();
        }
        tag.remove("main_base_layout");
        var old = (BuildingBlockEntity) BlockEntity.loadStatic(post.getBlockPos(), post.getBlockState(), tag, registry);
        h.assertTrue(old != null && old.isMainBase() && old.mainBaseLayoutLabel().equals("unversioned foundation")
                && !old.saveWithFullMetadata(registry).contains("main_base_layout"), "Old base is not adopted or relabelled");
        h.succeed();
    }
}
