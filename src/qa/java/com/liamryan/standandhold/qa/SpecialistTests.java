package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.common.threat.ThreatResponse;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.ResponseConfig;
import com.liamryan.standandhold.config.StructureConfig;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class SpecialistTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("division_site_research_fallback", SpecialistTests::gates);
        tests.put("division_shared_paths_unloaded", SpecialistTests::paths);
        tests.put("specialist_eggs_creative_only", SpecialistTests::eggs);
        tests.put("specialist_friendly_retention_no_drops", SpecialistTests::retention);
        return tests;
    }
    private static BuildingPosition home(GameTestHelper h, BuildingBlockEntity post) {
        return new BuildingPosition(h.getLevel().dimension().identifier(), post.getBlockPos());
    }
    private static BuildingBlockEntity post(GameTestHelper h, BlockPos relative, boolean base) {
        h.setBlock(relative, BuildingRegistry.COMMAND_POST.get());
        var post = h.getBlockEntity(relative, BuildingBlockEntity.class);
        if (base) {
            post.initializeMainBase();
            TierTests.data(h).registerMainBase(home(h, post));
        } else { TierTests.data(h).registerCommandPost(home(h, post)); }
        return post;
    }
    private static void train(GameTestHelper h) {
        var entry = FoundationResources.get(h.getLevel().getServer()).research("special_division_training");
        var supplied = new BuildingState(1, 100, 5, entry.id(), 12, true);
        var result = BuildingTransactions.finishResearch(supplied, TierTests.data(h), entry, HumanProgressionConfig.thresholds(), 12);
        h.assertTrue(result.supplies() == 0 && result.samples() == 0 && TierTests.data(h).isResearchComplete(entry.id()), "Real training transaction charges local inputs");
    }
    private static HumanNpc deploy(GameTestHelper h, BuildingBlockEntity post, HumanTier expected) {
        var unit = DefenderDeployment.attempt(h.getLevel(), post);
        h.assertTrue(unit != null && unit.tier() == expected, "Expected shared recruitment: " + expected);
        return unit;
    }
    private static void gates(GameTestHelper h) {
        QaWorld.begin(h); TierTests.unlock(h); TierTests.stage(h, 4);
        var base = post(h, HOME, true); var checkpoint = post(h, HOME.offset(12, 0, 0), false);
        h.runAfterDelay(3, () -> {
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), base) == null, "Dormant Main Base cannot deploy");
            deploy(h, checkpoint, HumanTier.SUPER_ELITE).discard();
            TierTests.stage(h, 5);
            deploy(h, base, HumanTier.SUPER_ELITE).discard();
            train(h);
            deploy(h, checkpoint, HumanTier.SUPER_ELITE).discard();
            var research = NpcConfig.SPECIAL_DIVISION.research;
            String previous = research.get();
            boolean enabled = NpcConfig.DIVISION_DEPLOY.get();
            int activeStage = StructureConfig.ACTIVATION_STAGE.get();
            try {
                for (String invalid : List.of("", "NOT AN ID", "standandhold:removed_training")) {
                    research.set(invalid); deploy(h, base, HumanTier.SUPER_ELITE).discard();
                }
                research.set(previous);
                NpcConfig.DIVISION_DEPLOY.set(false); deploy(h, base, HumanTier.SUPER_ELITE).discard();
                NpcConfig.DIVISION_DEPLOY.set(enabled);
                StructureConfig.ACTIVATION_STAGE.set(4); TierTests.stage(h, 4);
                deploy(h, base, HumanTier.SUPER_ELITE).discard();
            } finally {
                research.set(previous); NpcConfig.DIVISION_DEPLOY.set(enabled); StructureConfig.ACTIVATION_STAGE.set(activeStage);
            }
            TierTests.stage(h, 5);
            var first = deploy(h, base, HumanTier.SPECIAL_DIVISION);
            first.discard();
            deploy(h, base, HumanTier.SPECIAL_DIVISION);
            h.succeed();
        });
    }
    private static void paths(GameTestHelper h) {
        QaWorld.begin(h); TierTests.unlock(h); TierTests.stage(h, 5); train(h);
        var base = post(h, HOME, true); var home = home(h, base);
        int cooldown = ResponseConfig.REINFORCEMENT_COOLDOWN.get();
        ResponseConfig.REINFORCEMENT_COOLDOWN.set(200);
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo info) { }
            public void testPassed(GameTestInfo info, GameTestRunner runner) { ResponseConfig.REINFORCEMENT_COOLDOWN.set(cooldown); }
            public void testFailed(GameTestInfo info, GameTestRunner runner) { ResponseConfig.REINFORCEMENT_COOLDOWN.set(cooldown); }
            public void testAddedForRerun(GameTestInfo old, GameTestInfo copy, GameTestRunner runner) { }
        });
        var player = QaPlayer.join(h.getLevel()); var p = base.getBlockPos();
        player.snapTo(p.getX()+.5, p.getY(), p.getZ()+4.5, 180, 16);
        h.runAfterDelay(3, () -> {
            for (int i = 0; i < NpcConfig.INTERVAL.get(); i++) { BuildingBlockEntity.tick(h.getLevel(), p, base.getBlockState(), base); }
            var first = units(h).getFirst();
            h.assertTrue(first.tier() == HumanTier.SPECIAL_DIVISION && TierTests.data(h).hasDivisionDefender(home.key()), "Real tile interval recruits the sole Division member");
            first.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            h.assertTrue(TierTests.data(h).hasDivisionDefender(home.key()) && TierTests.data(h).hasDefender(home.key(), first.getUUID()), "Actual unload keeps the Division reservation");
            h.assertTrue(SupportCallIn.request(player, base).success() && units(h).getFirst().tier() == HumanTier.SUPER_ELITE,
                    "Paid path falls back while the Division member is unloaded");
            h.assertTrue(!SupportCallIn.request(player, base).success() && TierTests.data(h).supplies() == 190
                    && !ThreatResponse.deployReinforcement(h.getLevel(), base).success(), "Shared cost and cooldown retained");
        });
        h.runAfterDelay(210, () -> {
            h.assertTrue(ThreatResponse.deployReinforcement(h.getLevel(), base).success(), "Event path can fill remaining slot");
            h.assertTrue(units(h).size() == 2 && units(h).stream().allMatch(unit -> unit.tier() == HumanTier.SUPER_ELITE)
                    && TierTests.data(h).defenderCount(home.key()) == 3, "Event cannot replace the unloaded Division member or exceed ordinary capacity");
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), base) == null, "Combined roster remains capped");
            h.succeed();
        });
    }
    private static List<HumanNpc> units(GameTestHelper h) {
        return h.getLevel().getEntitiesOfClass(HumanNpc.class, new AABB(h.absolutePos(HOME)).inflate(24), Entity::isAlive);
    }
    private static void eggs(GameTestHelper h) {
        QaWorld.begin(h);
        var player = QaPlayer.join(h.getLevel());
        var floor = h.absolutePos(HOME.below());
        var hit = new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false);
        for (var egg : List.of(NpcRegistry.SUPER_ELITE_EGG.get(), NpcRegistry.SPECIAL_DIVISION_EGG.get())) {
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, egg.getDefaultInstance());
            h.assertTrue(egg.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)) == InteractionResult.FAIL, "Test egg refuses survival use");
            player.setGameMode(GameType.CREATIVE);
            h.assertTrue(egg.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)) == InteractionResult.SUCCESS, "Creative test bypasses recruitment only");
        }
        h.assertTrue(units(h).size() == 2 && units(h).stream().allMatch(npc -> npc.tier().advanced()
                && npc.commandPost().isEmpty() && npc.getMainHandItem().isEmpty()), "Eggs provide correct unassigned units, not equipment or reservations");
        for (var tier : List.of(HumanTier.SUPER_ELITE, HumanTier.SPECIAL_DIVISION)) {
            h.assertTrue(NpcRegistry.entityType(tier).spawn(h.getLevel(), h.absolutePos(HOME.offset(4, 0, 0)), EntitySpawnReason.DISPENSER) == null, "Automation cannot create " + tier);
        }
        h.succeed();
    }
    private static void retention(GameTestHelper h) {
        QaWorld.begin(h); TierTests.unlock(h); TierTests.stage(h, 5); train(h);
        var base = post(h, HOME, true);
        var cow = h.spawn(EntityTypes.COW, HOME.offset(0, 0, 4)); cow.setNoAi(true);
        var observer = QaPlayer.join(h.getLevel());
        h.runAfterDelay(3, () -> {
            var division = deploy(h, base, HumanTier.SPECIAL_DIVISION);
            var elite = deploy(h, base, HumanTier.SUPER_ELITE);
            h.assertFalse(division.canAttack(cow) || division.canAttack(observer) || division.canAttack(elite)
                    || elite.canAttack(cow) || elite.canAttack(observer) || elite.canAttack(division), "Both specialists retain friendly exclusions");
            h.runAfterDelay(45, () -> {
                h.assertTrue(division.getTarget() == null && elite.getTarget() == null && cow.getHealth() == cow.getMaxHealth(), "No friendly targeting");
                TierTests.stage(h, 0);
                h.assertTrue(division.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && elite.hasIssuedBlade(), "Real issued kits retained at lower stage");
                division.kill(h.getLevel()); elite.kill(h.getLevel());
                h.runAfterDelay(30, () -> {
                    h.assertTrue(TierTests.data(h).defenderCount(home(h, base).key()) == 0, "Death releases both normal and Division reservations");
                    h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(base.getBlockPos()).inflate(18)).isEmpty(), "Issued specialist kit does not drop");
                    h.succeed();
                });
            });
        });
    }
}
