package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.sample.*;
import com.liamryan.standandhold.common.structure.*;
import com.liamryan.standandhold.common.threat.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

final class WorldTests {
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("checkpoint_flat", h -> structure(h, StructureKind.CHECKPOINT, false));
        tests.put("checkpoint_lab_minimum", h -> checkpointLab(h, 3));
        tests.put("checkpoint_lab_maximum", h -> checkpointLab(h, 5));
        tests.put("occupied_checkpoint_lab", WorldTests::occupiedLab);
        tests.put("checkpoint_lab_research", WorldTests::checkpointResearch);
        tests.put("mainbase_flat", h -> structure(h, StructureKind.MAIN_BASE, false));
        tests.put("occupied_checkpoint", h -> structure(h, StructureKind.CHECKPOINT, true));
        tests.put("occupied_mainbase", h -> structure(h, StructureKind.MAIN_BASE, true));
        tests.put("raid_clear", WorldTests::raidClear);
        tests.put("raid_ambient_quota", WorldTests::raidQuota);
        tests.put("raid_unsafe_terrain", WorldTests::raidTerrain);
        tests.put("target_zombie", h -> target(h, EntityTypes.ZOMBIE));
        tests.put("target_skeleton", h -> target(h, EntityTypes.SKELETON));
        tests.put("target_creeper", h -> target(h, EntityTypes.CREEPER));
        tests.put("protect_players_animals", WorldTests::protect);
        tests.put("blade_issue_retention", WorldTests::blade);
        tests.put("lab_finishes_once", WorldTests::lab);
        tests.put("mainbase_notices_once", WorldTests::notices);
        tests.put("kill_rewards_sample_pickup", WorldTests::samples);
        tests.putAll(SupportTests.cases());
        tests.putAll(OutpostTests.cases());
        tests.putAll(PreviewTests.cases());
        tests.putAll(ExpansionTests.cases());
        return tests;
    }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static BuildingBlockEntity building(GameTestHelper h, BlockPos pos, net.minecraft.world.level.block.Block block) {
        h.setBlock(pos, block);
        return h.getBlockEntity(pos, BuildingBlockEntity.class);
    }
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    private static void structure(GameTestHelper h, StructureKind kind, boolean occupied) {
        QaWorld.begin(h);
        BlockPos anchor = h.absolutePos(HOME);
        int chunkX = anchor.getX() >> 4, chunkZ = anchor.getZ() >> 4;
        BlockPos obstruction = new BlockPos((chunkX << 4) + 8, anchor.getY(), (chunkZ << 4) + 8);
        if (occupied) {
            h.getLevel().setBlockAndUpdate(obstruction, Blocks.CHEST.defaultBlockState());
            ((ChestBlockEntity) h.getLevel().getBlockEntity(obstruction)).setItem(0, Items.DIAMOND.getDefaultInstance());
        }
        var result = StructurePlacement.place(h.getLevel(), kind, chunkX, chunkZ);
        if (occupied) {
            h.assertFalse(result.success(), "Occupied footprint must be refused");
            h.assertTrue(h.getLevel().getBlockEntity(obstruction) instanceof ChestBlockEntity chest && chest.getItem(0).is(Items.DIAMOND), "Occupied container/data must remain intact");
            h.assertTrue(data(h).commandPostKeys().isEmpty(), "Refusal must not register a partial site");
        } else {
            h.assertTrue(result.success(), "Dry superflat grass must place " + kind + ": " + result.message());
            var position = new BuildingPosition(h.getLevel().dimension().identifier(), result.commandPost());
            h.assertTrue(data(h).commandPostKeys().contains(position.key()), "Generated post registered");
            var post = (BuildingBlockEntity) h.getLevel().getBlockEntity(result.commandPost());
            h.assertTrue(post != null && post.isMainBase() == (kind == StructureKind.MAIN_BASE), "Correct command tile/base flag");
            h.assertTrue(kind != StructureKind.MAIN_BASE || data(h).isMainBase(position), "Main Base registered");
            if (kind == StructureKind.CHECKPOINT) {
                h.assertTrue(post.site().anchor().equals(result.commandPost()) && post.site().origin().equals("debug"), "New checkpoint receives generation provenance");
            } else {
                try { post.site(); h.fail("Main Base must not receive checkpoint provenance"); } catch (IllegalArgumentException expected) { }
            }
            int radius = kind == StructureKind.CHECKPOINT ? com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.get()
                    : com.liamryan.standandhold.config.StructureConfig.MAIN_BASE_RADIUS.get();
            var lab = (BuildingBlockEntity) h.getLevel().getBlockEntity(result.commandPost().offset(-radius + 2, 0, -radius + 3));
            h.assertTrue(lab != null && lab.kind() == BuildingKind.LAB && lab.state().equals(BuildingState.EMPTY), "Each new site has one empty, idle real lab tile");
        }
        h.runAfterDelay(5, h::succeed);
    }
    private static void checkpointLab(GameTestHelper h, int radius) {
        QaWorld.begin(h);
        var config = com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS;
        int saved = config.get();
        try {
            config.set(radius);
            BlockPos anchor = h.absolutePos(HOME);
            var result = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, anchor.getX() >> 4, anchor.getZ() >> 4);
            h.assertTrue(result.success(), "Checkpoint radius " + radius + " placed: " + result.message());
            BlockPos pos = result.commandPost().offset(-radius + 2, 0, -radius + 3);
            h.assertTrue(h.getLevel().getBlockEntity(pos) instanceof BuildingBlockEntity lab && lab.kind() == BuildingKind.LAB
                    && lab.state().equals(BuildingState.EMPTY), "Empty generated lab exists at radius " + radius);
            h.assertTrue(h.getLevel().getBlockState(pos.south()).isAir() && h.getLevel().getBlockState(pos.above()).isAir(), "Lab access/headroom remain clear");
            h.assertTrue(data(h).commandPostKeys().size() == 1, "Lab adds no extra post/base registry entry");
        } finally { config.set(saved); }
        h.runAfterDelay(5, h::succeed);
    }
    private static void occupiedLab(GameTestHelper h) {
        QaWorld.begin(h);
        BlockPos anchor = h.absolutePos(HOME);
        int cx = anchor.getX() >> 4, cz = anchor.getZ() >> 4;
        int radius = com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.get();
        BlockPos pos = new BlockPos((cx << 4) + 10 - radius, anchor.getY(), (cz << 4) + 10 - radius);
        h.getLevel().setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        ((ChestBlockEntity) h.getLevel().getBlockEntity(pos)).setItem(0, Items.DIAMOND.getDefaultInstance());
        var result = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, cx, cz);
        h.assertFalse(result.success(), "Chest at proposed lab refuses entire checkpoint");
        h.assertTrue(h.getLevel().getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.getItem(0).is(Items.DIAMOND), "Chest contents retained");
        h.assertTrue(data(h).commandPostKeys().isEmpty(), "No partial registration on lab obstruction");
        h.runAfterDelay(5, h::succeed);
    }
    private static void checkpointResearch(GameTestHelper h) {
        QaWorld.begin(h);
        BlockPos anchor = h.absolutePos(HOME);
        var result = StructurePlacement.place(h.getLevel(), StructureKind.CHECKPOINT, anchor.getX() >> 4, anchor.getZ() >> 4);
        h.assertTrue(result.success(), "Checkpoint available for lab interaction: " + result.message());
        int radius = com.liamryan.standandhold.config.StructureConfig.CHECKPOINT_RADIUS.get();
        var lab = (BuildingBlockEntity) h.getLevel().getBlockEntity(result.commandPost().offset(-radius + 2, 0, -radius + 3));
        var observer = QaPlayer.join(h.getLevel()); var pos = lab.getBlockPos();
        observer.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.5, 180, 16);
        h.runAfterDelay(3, () -> {
            h.assertTrue(lab.state().equals(BuildingState.EMPTY), "Generated lab does not auto-start or grant resources");
            data(h).setSupplies(10);
            var menu = new BuildingMenu(1, observer.getInventory(), lab, FoundationResources.get(h.getLevel().getServer()));
            observer.containerMenu = menu;
            h.assertTrue(menu.clickMenuButton(observer, BuildingMenu.IMPORT), "Generated lab accepts actual server import action");
            lab.change(lab.state().target("standandhold:parasite_samples"));
            h.assertTrue(menu.clickMenuButton(observer, BuildingMenu.TOGGLE_RESEARCH), "Generated lab accepts explicit Start");
            int ticks = com.liamryan.standandhold.config.BuildingConfig.LAB_INTERVAL.get()
                    * (com.liamryan.standandhold.config.BuildingConfig.LAB_WORK.get() + 3);
            for (int i = 0; i < ticks; i++) { BuildingBlockEntity.tick(h.getLevel(), pos, lab.getBlockState(), lab); }
            h.assertTrue(data(h).isResearchComplete("standandhold:parasite_samples") && data(h).points() == 10
                    && data(h).supplies() == 0 && lab.state().supplies() == 0, "Generated lab spends imported supplies and rewards exactly once");
            h.assertTrue(!lab.state().researching() && observer.messages("message.standandhold.research_complete") == 1, "Generated lab completes/stops with exactly one notice");
            h.succeed();
        });
    }
    private static void raidClear(GameTestHelper h) {
        QaWorld.begin(h);
        var post = building(h, HOME, BuildingRegistry.COMMAND_POST.get());
        h.runAfterDelay(3, () -> {
            String result = ThreatResponse.raid(h.getLevel(), post);
            h.assertTrue(result.startsWith("Outpost raid"), "Clear dry arena must spawn: " + result);
            var state = ThreatResponse.state(h.getLevel(), post);
            h.assertTrue(state.raids() == 1 && state.raiders().size() == 1, "Stage 0 creates exactly one reserved raider");
            var raiders = h.getLevel().getEntities(EntityTypes.ZOMBIE, new AABB(post.getBlockPos()).inflate(16), z -> z.entityTags().contains(ThreatResponse.RAID_TAG));
            h.assertTrue(raiders.size() == 1, "Actual zombie, not only ledger increment");
            h.assertTrue(ThreatResponse.raid(h.getLevel(), post).contains("already reserved"), "Repeat must not spam waves");
            h.succeed();
        });
    }
    private static void raidQuota(GameTestHelper h) {
        QaWorld.begin(h);
        var post = building(h, HOME, BuildingRegistry.COMMAND_POST.get());
        for (int i = 0; i < 8; i++) {
            var slime = h.spawn(EntityTypes.SLIME, new BlockPos(28 + i % 4, 2, 28 + i / 4));
            slime.setSize(1, true); slime.setNoAi(true); slime.setPersistenceRequired();
        }
        h.runAfterDelay(3, () -> {
            String result = ThreatResponse.raid(h.getLevel(), post);
            h.assertTrue(result.contains("hostile quota"), "Ambient mobs must explain quota refusal: " + result);
            var state = ThreatResponse.state(h.getLevel(), post);
            h.assertTrue(state.raids() == 0 && state.raiders().isEmpty(), "Refusal has no phantom raiders");
            h.assertTrue(state.nextRaid() > h.getLevel().getGameTime(), "Failed spawn backoff saved");
            h.succeed();
        });
    }
    private static void raidTerrain(GameTestHelper h) {
        QaWorld.begin(h);
        var post = building(h, HOME, BuildingRegistry.COMMAND_POST.get());
        for (BlockPos pos : BlockPos.betweenClosed(20, -1, 20, 44, 1, 44)) { h.setBlock(pos, Blocks.AIR); }
        h.runAfterDelay(3, () -> {
            String result = ThreatResponse.raid(h.getLevel(), post);
            h.assertTrue(result.contains("no safe loaded spawn"), "Unsafe ground must explain terrain refusal: " + result);
            h.assertTrue(ThreatResponse.state(h.getLevel(), post).raiders().isEmpty(), "No reservation on unsafe terrain");
            h.succeed();
        });
    }
    private static <T extends Mob> void target(GameTestHelper h, net.minecraft.world.entity.EntityType<T> type) {
        QaWorld.begin(h);
        var soldier = h.spawn(NpcRegistry.SURVIVOR.get(), HOME);
        var hostile = h.spawn(type, HOME.offset(4, 0, 0));
        hostile.setNoAi(true); hostile.setPersistenceRequired();
        float health = hostile.getHealth();
        h.succeedWhen(() -> h.assertTrue(hostile.getHealth() < health || !hostile.isAlive(), "Defender must independently acquire and damage " + type));
    }
    private static void protect(GameTestHelper h) {
        QaWorld.begin(h);
        var soldier = h.spawn(NpcRegistry.SURVIVOR.get(), HOME);
        var cow = h.spawn(EntityTypes.COW, HOME.offset(2, 0, 0)); cow.setNoAi(true);
        var pig = h.spawn(EntityTypes.PIG, HOME.offset(0, 0, 2)); pig.setNoAi(true);
        var player = QaPlayer.join(h.getLevel());
        BlockPos pos = h.absolutePos(HOME.offset(-2, 0, 0)); player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        float playerHealth = player.getHealth(), cowHealth = cow.getHealth(), pigHealth = pig.getHealth();
        soldier.hurtServer(h.getLevel(), h.getLevel().damageSources().playerAttack(player), 1);
        h.runAfterDelay(100, () -> {
            h.assertFalse(soldier.canAttack(player) || soldier.canAttack(cow) || soldier.canAttack(pig), "Players/animals are not valid NPC targets");
            h.assertTrue(soldier.getTarget() == null && player.getHealth() == playerHealth && cow.getHealth() == cowHealth && pig.getHealth() == pigHealth, "No attacks or retaliation against civilians");
            h.succeed();
        });
    }
    private static void blade(GameTestHelper h) {
        QaWorld.begin(h);
        var first = h.spawn(NpcRegistry.SURVIVOR.get(), HOME);
        h.runAfterDelay(40, () -> {
            h.assertTrue(first.getMainHandItem().isEmpty(), "Stage 0 survivor starts unarmed");
            data(h).setSupplies(10);
            ResearchManager.complete(data(h), FoundationResources.get(h.getLevel().getServer()), HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
            HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.LOCAL_ARMY_RESPONSE);
        });
        h.runAfterDelay(80, () -> {
            h.assertTrue(first.hasIssuedBlade() && first.getMainHandItem().is(EquipmentRegistry.BLADE.get()), "Stage 1/research issues blade");
            HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.SURVIVORS);
            var fresh = h.spawn(NpcRegistry.SURVIVOR.get(), HOME.offset(4, 0, 0));
            h.runAfterDelay(40, () -> {
                h.assertTrue(first.hasIssuedBlade(), "Issued blade retained after stage drop");
                h.assertTrue(fresh.getMainHandItem().isEmpty(), "New Stage 0 unit cannot inherit entitlement");
                h.succeed();
            });
        });
    }
    private static void lab(GameTestHelper h) {
        QaWorld.begin(h);
        var observer = QaPlayer.join(h.getLevel());
        var lab = building(h, HOME, BuildingRegistry.LAB.get());
        lab.change(BuildingState.EMPTY.supplies(10).target("standandhold:parasite_samples").running(true));
        h.runAfterDelay(1500, () -> {
            h.assertTrue(data(h).isResearchComplete("standandhold:parasite_samples"), "Real lab ticks completed research");
            h.assertTrue(data(h).points() == 10 && lab.state().supplies() == 0 && lab.state().work() == 12, "Exactly one local cost and world reward");
            h.assertTrue(!lab.state().researching() && observer.messages("message.standandhold.research_complete") == 1, "One completion message and stopped lab");
            h.succeed();
        });
    }
    private static void notices(GameTestHelper h) {
        QaWorld.begin(h);
        var post = building(h, HOME, BuildingRegistry.COMMAND_POST.get()); post.initializeMainBase();
        data(h).registerMainBase(new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos()));
        var player = QaPlayer.join(h.getLevel());
        BlockPos pos = h.absolutePos(HOME.offset(2, 0, 0)); player.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        h.runAfterDelay(3, () -> {
            h.useBlock(HOME, player); h.useBlock(HOME, player);
            h.assertTrue(player.messages("message.standandhold.main_base_dormant") == 1, "Dormant notice once per base");
            HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.SPECIAL_PARASITE_DIVISION);
            h.useBlock(HOME, player); h.useBlock(HOME, player);
            h.assertTrue(player.messages("message.standandhold.main_base_active") == 1, "Active notice once per base");
            var loaded = (BuildingBlockEntity) BlockEntity.loadStatic(post.getBlockPos(), post.getBlockState(), post.saveWithFullMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
            h.assertTrue(loaded != null && !loaded.claimMainBaseNotice(false) && !loaded.claimMainBaseNotice(true), "Both notice receipts survive NBT load");
            h.succeed();
        });
    }
    private static void samples(GameTestHelper h) {
        QaWorld.begin(h);
        var player = QaPlayer.join(h.getLevel());
        BlockPos pos = h.absolutePos(HOME); player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        var catalog = FoundationResources.get(h.getLevel().getServer());
        MissionManager.start(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:recover_parasite_samples");
        h.getLevel().getRandom().setSeed(918273L);
        for (int i = 0; i < 24; i++) {
            Zombie zombie = h.spawn(EntityTypes.ZOMBIE, HOME.offset(2, 0, 0));
            zombie.hurtServer(h.getLevel(), h.getLevel().damageSources().playerAttack(player), 1000);
        }
        h.assertTrue(data(h).points() == 120, "Actual player-attributed death events award configured points");
        var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, h.getBounds(), item -> item.getItem().is(SampleItems.TISSUE.get()));
        h.assertTrue(drops.stream().mapToInt(item -> item.getItem().getCount()).sum() >= 3, "Seeded actual zombie drops provide samples");
        for (var item : drops) { item.setNoPickUpDelay(); item.playerTouch(player); }
        h.assertTrue(SampleInventory.count(player.getInventory()) >= 3 && data(h).mission("standandhold:recover_parasite_samples").orElseThrow().completed(), "Real pickup hook completes recovery mission");
        h.assertTrue(data(h).points() == 145 && data(h).supplies() == 10, "Recovery reward applied once");
        SampleEvents.observe(player); SampleEvents.observe(player);
        h.assertTrue(data(h).points() == 145 && data(h).supplies() == 10, "Repeated pickup observation cannot duplicate reward");
        h.runAfterDelay(5, h::succeed);
    }
}
