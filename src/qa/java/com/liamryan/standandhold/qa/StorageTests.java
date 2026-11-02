package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

final class StorageTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("storage_legacy_flat", StorageTests::flat);
        tests.put("storage_exact_cap_competing", StorageTests::competing);
        tests.put("storage_upgrade_conservation", StorageTests::upgrade);
        tests.put("storage_generation_limits", StorageTests::generation);
        tests.put("storage_lowered_limits_export", StorageTests::lowered);
        tests.put("storage_lab_crate_independent", StorageTests::independent);
        tests.put("storage_unknown_save", StorageTests::unknown);
        tests.put("storage_overflow_authority", StorageTests::overflow);
        tests.put("storage_malformed_profile", StorageTests::malformed);
        var scoped = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.forEach((name, test) -> scoped.put(name, h -> {
            boolean mode = BuildingConfig.LEVEL_STORAGE.get(); var profile = BuildingConfig.STORAGE_LEVELS.get();
            int flat = BuildingConfig.CAPACITY.get(), points = BuildingConfig.POINTS.get(), supplies = BuildingConfig.SUPPLIES.get();
            int interval = BuildingConfig.INTERVAL.get(), batch = BuildingConfig.TRANSFER.get();
            h.testInfo.addListener(new GameTestListener() {
                private void restore() {
                    BuildingConfig.LEVEL_STORAGE.set(mode); BuildingConfig.STORAGE_LEVELS.set(profile); BuildingConfig.CAPACITY.set(flat);
                    BuildingConfig.POINTS.set(points); BuildingConfig.SUPPLIES.set(supplies); BuildingConfig.INTERVAL.set(interval); BuildingConfig.TRANSFER.set(batch);
                }
                public void testStructureLoaded(GameTestInfo i) { }
                public void testPassed(GameTestInfo i, GameTestRunner r) { restore(); }
                public void testFailed(GameTestInfo i, GameTestRunner r) { restore(); }
                public void testAddedForRerun(GameTestInfo a, GameTestInfo b, GameTestRunner r) { }
            });
            BuildingConfig.LEVEL_STORAGE.set(true); BuildingConfig.STORAGE_LEVELS.set(List.of(20, 40, 60, 80, 100));
            BuildingConfig.CAPACITY.set(17); BuildingConfig.POINTS.set(0); BuildingConfig.SUPPLIES.set(3);
            BuildingConfig.INTERVAL.set(20); BuildingConfig.TRANSFER.set(10);
            QaWorld.begin(h); test.accept(h);
        }));
        return scoped;
    }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static BuildingBlockEntity post(GameTestHelper h) { h.setBlock(HOME, BuildingRegistry.COMMAND_POST.get()); return h.getBlockEntity(HOME, BuildingBlockEntity.class); }
    private static BuildingMenu menu(GameTestHelper h, BuildingBlockEntity post, ServerPlayer actor, int id) {
        var p = post.getBlockPos(); actor.snapTo(p.getX() + .5, p.getY(), p.getZ() + 4.5, 180, 16);
        var menu = new BuildingMenu(id, actor.getInventory(), post, FoundationResources.get(h.getLevel().getServer())); actor.containerMenu = menu; return menu;
    }
    private static void cycle(GameTestHelper h, BuildingBlockEntity post) {
        for (int i = 0; i < BuildingConfig.INTERVAL.get(); i++) { BuildingBlockEntity.tick(h.getLevel(), post.getBlockPos(), post.getBlockState(), post); }
    }
    private static void flat(GameTestHelper h) {
        var post = post(h); BuildingConfig.LEVEL_STORAGE.set(false); data(h).setSupplies(30);
        var actor = QaPlayer.join(h.getLevel()); var menu = menu(h, post, actor, 1);
        h.assertTrue(post.supplyCapacity() == 17 && menu.number(BuildingMenu.NEXT_CAPACITY) == 17 && menu.number(BuildingMenu.LEVEL_STORAGE) == 0, "Explicit flat setting remains authoritative at every level");
        h.assertTrue(menu.clickMenuButton(actor, BuildingMenu.IMPORT), "One whole batch fits legacy capacity");
        h.assertFalse(menu.clickMenuButton(actor, BuildingMenu.IMPORT), "Seven remaining spaces cannot accept ten supplies");
        post.change(new BuildingState(5, 10, 0, "", 0));
        h.assertTrue(post.supplyCapacity() == 17 && data(h).supplies() == 20, "Level change does not reinterpret flat storage or refill balances"); h.succeed();
    }
    private static void competing(GameTestHelper h) {
        var post = post(h); data(h).setSupplies(30);
        var a = QaPlayer.join(h.getLevel()); var b = QaPlayer.join(h.getLevel()); var first = menu(h, post, a, 1); var second = menu(h, post, b, 2);
        h.assertTrue(first.clickMenuButton(a, BuildingMenu.IMPORT) && second.clickMenuButton(b, BuildingMenu.IMPORT), "Two sequential server actions exactly fill capacity");
        h.assertFalse(first.clickMenuButton(a, BuildingMenu.IMPORT), "Stale first menu cannot exceed current capacity");
        h.assertTrue(second.clickMenuButton(b, BuildingMenu.EXPORT) && first.clickMenuButton(a, BuildingMenu.IMPORT), "Interleaved actions recheck current local/global balances");
        h.assertTrue(post.state().supplies() == 20 && data(h).supplies() == 10, "All thirty supplies conserved"); h.succeed();
    }
    private static void upgrade(GameTestHelper h) {
        var post = post(h); data(h).setSupplies(100);
        ResearchManager.complete(data(h), FoundationResources.get(h.getLevel().getServer()), HumanProgressionConfig.thresholds(), "parasite_samples");
        HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.LOCAL_ARMY_RESPONSE);
        post.change(post.state().supplies(20)); long global = data(h).supplies(), points = data(h).points();
        var a = QaPlayer.join(h.getLevel()); var b = QaPlayer.join(h.getLevel()); var first = menu(h, post, a, 1); var second = menu(h, post, b, 2);
        h.assertTrue(first.number(BuildingMenu.CAPACITY) == 20 && first.number(BuildingMenu.NEXT_CAPACITY) == 40, "Menu predicts the same derived level benefit");
        h.assertTrue(first.clickMenuButton(a, BuildingMenu.UPGRADE), "Existing upgrade transaction buys level two");
        h.assertFalse(second.clickMenuButton(b, BuildingMenu.UPGRADE), "Competing stale upgrade cannot repeat an unaffordable purchase");
        second.broadcastChanges();
        h.assertTrue(post.state().level() == 2 && post.state().supplies() == 0 && post.supplyCapacity() == 40
                && second.number(BuildingMenu.CAPACITY) == 40 && second.number(BuildingMenu.NEXT_CAPACITY) == 60, "Higher capacity starts empty and syncs live");
        h.assertTrue(data(h).points() == points && data(h).supplies() == global, "Upgrade spends only twenty local supplies, not global currency/points"); h.succeed();
    }
    private static void generation(GameTestHelper h) {
        var post = post(h); post.change(post.state().supplies(19)); cycle(h, post);
        h.assertTrue(post.state().supplies() == 20, "One generation cycle uses exactly remaining room"); cycle(h, post);
        h.assertTrue(post.state().supplies() == 20, "Full cycle adds nothing");
        post.change(new BuildingState(2, 20, 0, "", 0)); cycle(h, post);
        h.assertTrue(post.state().supplies() == 23 && post.supplyCapacity() == 40, "Level adds room but the same configured reward of three");
        post.change(post.state().supplies(Long.MAX_VALUE)); cycle(h, post);
        h.assertTrue(post.state().supplies() == Long.MAX_VALUE, "Over-cap long balance survives generation unchanged"); h.succeed();
    }
    private static void lowered(GameTestHelper h) {
        var post = post(h); post.change(new BuildingState(3, 59, 2, "", 0));
        post.change(new BuildingState(1, 59, 2, "", 0));
        h.assertTrue(post.supplyCapacity() == 20 && post.state().supplies() == 59, "Lower local level never clamps saved supplies");
        BuildingConfig.LEVEL_STORAGE.set(false); cycle(h, post);
        var actor = QaPlayer.join(h.getLevel()); var menu = menu(h, post, actor, 1);
        h.assertTrue(post.supplyCapacity() == 17 && post.state().supplies() == 59 && !menu.clickMenuButton(actor, BuildingMenu.IMPORT), "Disabling profile keeps excess and stops imports/generation");
        h.assertTrue(menu.clickMenuButton(actor, BuildingMenu.EXPORT) && post.state().supplies() == 49 && data(h).supplies() == 10, "Over-cap export remains available");
        BuildingConfig.LEVEL_STORAGE.set(true); BuildingConfig.STORAGE_LEVELS.set(List.of(10, 11, 12, 13, 14)); cycle(h, post);
        h.assertTrue(post.state().supplies() == 49 && post.supplyCapacity() == 10, "Lower profile is a limit, not a destructive balance rewrite"); h.succeed();
    }
    private static void independent(GameTestHelper h) {
        var actor = QaPlayer.join(h.getLevel()); data(h).setSupplies(50);
        int index = 0;
        for (var block : List.of(BuildingRegistry.LAB.get(), BuildingRegistry.CRATE.get())) {
            var pos = HOME.offset(index++ * 4, 0, 0); h.setBlock(pos, block); var tile = h.getBlockEntity(pos, BuildingBlockEntity.class);
            tile.change(new BuildingState(5, 7, 0, "", 0)); var menu = menu(h, tile, actor, index);
            h.assertTrue(tile.supplyCapacity() == 17 && menu.clickMenuButton(actor, BuildingMenu.IMPORT) && tile.state().supplies() == 17, "Lab/crate keep independent flat capacity even at saved level five");
            h.assertFalse(menu.clickMenuButton(actor, BuildingMenu.IMPORT), "Profile never grants lab/crate room");
        }
        h.assertTrue(data(h).supplies() == 30, "Only exact deposits change shared supplies"); h.succeed();
    }
    private static void unknown(GameTestHelper h) {
        var post = post(h); post.change(new BuildingState(3, 6000, 2, "", 0)); var tag = post.saveWithFullMetadata(h.getLevel().registryAccess());
        tag.putInt("building_version", 99); tag.getCompoundOrEmpty("building").putString("future_note", "keep");
        post = (BuildingBlockEntity) BlockEntity.loadStatic(post.getBlockPos(), post.getBlockState(), tag, h.getLevel().registryAccess());
        h.assertTrue(post != null, "Future tile still loads"); h.getLevel().setBlockEntity(post);
        var actor = QaPlayer.join(h.getLevel()); var menu = menu(h, post, actor, 1); var saved = post.saveWithFullMetadata(h.getLevel().registryAccess());
        h.assertFalse(menu.clickMenuButton(actor, BuildingMenu.IMPORT) || menu.clickMenuButton(actor, BuildingMenu.EXPORT) || menu.clickMenuButton(actor, BuildingMenu.UPGRADE), "Unknown saves never authorize storage or upgrade actions");
        cycle(h, post);
        h.assertTrue(post.saveWithFullMetadata(h.getLevel().registryAccess()).equals(saved) && data(h).supplies() == 0, "Capacity adds no migration, clamp or rewrite of unknown data"); h.succeed();
    }
    private static void overflow(GameTestHelper h) {
        var post = post(h); post.change(post.state().supplies(Long.MAX_VALUE)); data(h).setSupplies(Long.MAX_VALUE);
        var actor = QaPlayer.join(h.getLevel()); var other = QaPlayer.join(h.getLevel()); var menu = menu(h, post, actor, 1);
        h.assertFalse(menu.clickMenuButton(actor, BuildingMenu.EXPORT) || menu.clickMenuButton(actor, BuildingMenu.IMPORT)
                || menu.clickMenuButton(other, BuildingMenu.EXPORT), "Overflow and foreign menu actions refused without partial writes");
        h.assertTrue(post.state().supplies() == Long.MAX_VALUE && data(h).supplies() == Long.MAX_VALUE, "Both long balances conserved"); h.succeed();
    }
    private static void malformed(GameTestHelper h) {
        var post = post(h); post.change(post.state().supplies(53)); BuildingConfig.STORAGE_LEVELS.set(List.of(1, 2, 3, 2, 5));
        var actor = QaPlayer.join(h.getLevel()); var menu = menu(h, post, actor, 1); cycle(h, post);
        h.assertTrue(post.supplyCapacity() == 17 && menu.number(BuildingMenu.CAPACITY) == 17 && menu.number(BuildingMenu.LEVEL_STORAGE) == 0
                && post.state().supplies() == 53, "Invalid runtime profile uses the flat setting without deleting excess");
        h.assertTrue(menu.clickMenuButton(actor, BuildingMenu.EXPORT) && post.state().supplies() == 43 && data(h).supplies() == 10, "Malformed profile fallback still permits safe withdrawals"); h.succeed();
    }
}
