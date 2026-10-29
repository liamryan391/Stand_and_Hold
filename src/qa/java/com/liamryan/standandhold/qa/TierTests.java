package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.progression.*;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.common.threat.ThreatResponse;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

final class TierTests {
    private static final BlockPos HOME = new BlockPos(32, 2, 32);
    static Map<String, Consumer<GameTestHelper>> cases() {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("tier_recruitment_gates", TierTests::gates);
        tests.put("tier_stats_and_eggs", TierTests::stats);
        tests.put("tier_configured_stats", TierTests::configuredStats);
        tests.put("tier_heavy_deployment_paths", h -> paths(h, HumanTier.HEAVY_SOLDIER));
        tests.put("tier_elite_deployment_paths", h -> paths(h, HumanTier.ELITE_SOLDIER));
        tests.put("tier_mixed_shared_caps", TierTests::caps);
        tests.put("tier_kit_retention", TierTests::retention);
        tests.put("tier_elite_ranged_bolt", TierTests::ranged);
        for (var tier : List.of(HumanTier.HEAVY_SOLDIER, HumanTier.ELITE_SOLDIER)) {
            tests.put("tier_" + tier.id + "_zombie", h -> combat(h, tier, EntityTypes.ZOMBIE));
            tests.put("tier_" + tier.id + "_skeleton", h -> combat(h, tier, EntityTypes.SKELETON));
            tests.put("tier_" + tier.id + "_creeper", h -> combat(h, tier, EntityTypes.CREEPER));
        }
        tests.put("tier_protect_civilians", TierTests::protect);
        return tests;
    }
    private static HumanWorldData data(GameTestHelper h) { return HumanWorldData.get(h.getLevel().getServer()); }
    private static void stage(GameTestHelper h, int stage) { HumanPointManager.setStage(h.getLevel().getServer(), HumanStage.fromLevel(stage)); }
    private static void unlock(GameTestHelper h) {
        data(h).setSupplies(200); stage(h, 1);
        var catalog = FoundationResources.get(h.getLevel().getServer());
        ResearchManager.complete(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:parasite_samples");
        ResearchManager.complete(data(h), catalog, HumanProgressionConfig.thresholds(), "standandhold:field_communications");
        data(h).setSupplies(200);
    }
    private static void gates(GameTestHelper h) {
        QaWorld.begin(h);
        for (int s = 0; s <= 6; s++) {
            stage(h, s);
            h.assertTrue(DefenderDeployment.eligibleTier(h.getLevel()) == (s == 0 ? HumanTier.SURVIVOR_DEFENDER : HumanTier.ARMY_RIFLEMAN), "Research cannot be bypassed by high stage");
        }
        unlock(h);
        for (int s = 0; s <= 6; s++) {
            stage(h, s);
            var expected = HumanTier.values()[Math.min(s, 3)];
            h.assertTrue(DefenderDeployment.eligibleTier(h.getLevel()) == expected, "Exact boundary for " + expected);
        }
        String heavy = NpcConfig.HEAVY.research.get(), elite = NpcConfig.ELITE.research.get(); int eliteStage = NpcConfig.ELITE.stage.get();
        try {
            for (String invalid : List.of("", "NOT AN ID", "missing:removed_research")) {
                NpcConfig.ELITE.research.set(invalid);
                h.assertTrue(DefenderDeployment.eligibleTier(h.getLevel()) == HumanTier.HEAVY_SOLDIER, "Invalid elite research falls back safely");
                NpcConfig.HEAVY.research.set(invalid);
                h.assertTrue(DefenderDeployment.eligibleTier(h.getLevel()) == HumanTier.ARMY_RIFLEMAN, "Both missing gates fall back to Army");
                NpcConfig.HEAVY.research.set(heavy);
            }
            NpcConfig.ELITE.research.set(elite); NpcConfig.ELITE.stage.set(5); stage(h, 3);
            h.assertTrue(DefenderDeployment.eligibleTier(h.getLevel()) == HumanTier.HEAVY_SOLDIER, "Configured stage honored");
        } finally { NpcConfig.HEAVY.research.set(heavy); NpcConfig.ELITE.research.set(elite); NpcConfig.ELITE.stage.set(eliteStage); }
        h.succeed();
    }
    private static void stats(GameTestHelper h) {
        QaWorld.begin(h);
        for (HumanTier tier : HumanTier.values()) {
            var npc = h.spawn(NpcRegistry.entityType(tier), HOME.offset(tier.ordinal() * 3 - 4, 0, 0)); npc.setNoAi(true);
            var config = NpcConfig.stats(tier);
            h.assertTrue(npc.tier() == tier && npc.getMaxHealth() == config.health.get().floatValue()
                    && npc.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == config.damage.get()
                    && npc.getAttributeBaseValue(Attributes.ARMOR) == config.armor.get()
                    && npc.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == config.speed.get(), "Correct configured type/stats even while recruitment locked: " + tier);
        }
        for (var egg : List.of(NpcRegistry.HEAVY_EGG.get(), NpcRegistry.ELITE_EGG.get())) {
            var player = QaPlayer.join(h.getLevel()); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, egg.getDefaultInstance());
            var floor = h.absolutePos(HOME.offset(0, -1, -6));
            egg.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(floor), net.minecraft.core.Direction.UP, floor, false)));
            var type = egg == NpcRegistry.HEAVY_EGG.get() ? NpcRegistry.HEAVY.get() : NpcRegistry.ELITE.get();
            h.assertTrue(humans(h).stream().filter(n -> n.getType() == type).count() == 2, "Actual egg spawns correct type at locked stage zero");
        }
        h.succeed();
    }
    private static BuildingBlockEntity post(GameTestHelper h) {
        h.setBlock(HOME, BuildingRegistry.COMMAND_POST.get()); return h.getBlockEntity(HOME, BuildingBlockEntity.class);
    }
    private static void configuredStats(GameTestHelper h) {
        QaWorld.begin(h); var config = NpcConfig.HEAVY;
        double health = config.health.get(), damage = config.damage.get(), speed = config.speed.get(), armor = config.armor.get();
        try {
            config.health.set(59.0); config.damage.set(9.0); config.speed.set(.21); config.armor.set(12.0);
            var npc = h.spawn(NpcRegistry.HEAVY.get(), HOME); npc.setNoAi(true);
            h.assertTrue(npc.getMaxHealth() == 59 && npc.getHealth() == 59 && npc.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 9
                    && npc.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == .21 && npc.getAttributeBaseValue(Attributes.ARMOR) == 12,
                    "Per-tier server config actually controls spawned attributes");
        } finally { config.health.set(health); config.damage.set(damage); config.speed.set(speed); config.armor.set(armor); }
        h.succeed();
    }
    private static List<HumanNpc> humans(GameTestHelper h) { return h.getLevel().getEntitiesOfClass(HumanNpc.class, new AABB(h.absolutePos(HOME)).inflate(30)); }
    private static void paths(GameTestHelper h, HumanTier tier) {
        QaWorld.begin(h); var post = post(h); unlock(h); stage(h, tier.stage);
        int cooldown = ResponseConfig.REINFORCEMENT_COOLDOWN.get(); ResponseConfig.REINFORCEMENT_COOLDOWN.set(200);
        h.testInfo.addListener(new net.minecraft.gametest.framework.GameTestListener() {
            private void restore() { ResponseConfig.REINFORCEMENT_COOLDOWN.set(cooldown); }
            public void testStructureLoaded(net.minecraft.gametest.framework.GameTestInfo info) { }
            public void testPassed(net.minecraft.gametest.framework.GameTestInfo info, net.minecraft.gametest.framework.GameTestRunner runner) { restore(); }
            public void testFailed(net.minecraft.gametest.framework.GameTestInfo info, net.minecraft.gametest.framework.GameTestRunner runner) { restore(); }
            public void testAddedForRerun(net.minecraft.gametest.framework.GameTestInfo old, net.minecraft.gametest.framework.GameTestInfo copy, net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
        var player = QaPlayer.join(h.getLevel()); var p = post.getBlockPos(); player.snapTo(p.getX()+.5, p.getY(), p.getZ()+4.5, 180, 16); player.setYHeadRot(180);
        h.runAfterDelay(3, () -> {
            for (int i = 0; i < NpcConfig.INTERVAL.get(); i++) { BuildingBlockEntity.tick(h.getLevel(), p, post.getBlockState(), post); }
            h.assertTrue(humans(h).size() == 1 && humans(h).getFirst().tier() == tier, "Real tile interval deploys " + tier);
            h.assertTrue(SupportCallIn.request(player, post).success(), "Paid path deploys eligible role");
            h.assertTrue(!SupportCallIn.request(player, post).success() && data(h).supplies() == 190, "Repeat respects shared cooldown/payment");
            h.assertTrue(!ThreatResponse.deployReinforcement(h.getLevel(), post).success(), "Event shares the paid cooldown");
        });
        h.runAfterDelay(210, () -> {
            h.assertTrue(ThreatResponse.deployReinforcement(h.getLevel(), post).success(), "Event shared path fills last slot");
            var units = humans(h);
            h.assertTrue(units.size() == 3 && units.stream().allMatch(n -> n.tier() == tier), "All paths agree on type without extra capacity");
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null, "Level-1 three-slot cap unchanged");
            stage(h, 0);
            h.assertTrue(units.stream().allMatch(n -> n.tier() == tier && n.isAlive()), "No automatic demotion/deletion"); h.succeed();
        });
    }
    private static void caps(GameTestHelper h) {
        QaWorld.begin(h); var post = post(h); unlock(h); stage(h, 6);
        for (int i = 0; i < 8; i++) { h.spawn(NpcRegistry.entityType(HumanTier.values()[i % 4]), HOME.offset(-5 + i % 4 * 3, 0, -5 + i / 4 * 3)).setNoAi(true); }
        h.runAfterDelay(3, () -> {
            int chunks = h.getLevel().getChunkSource().getLoadedChunksCount();
            h.assertTrue(DefenderDeployment.attempt(h.getLevel(), post) == null && !ThreatResponse.deployReinforcement(h.getLevel(), post).success(), "Mixed eggs count toward the same area cap");
            h.assertTrue(chunks == h.getLevel().getChunkSource().getLoadedChunksCount(), "Tier selection/spawn does not load chunks"); h.succeed();
        });
    }
    private static void retention(GameTestHelper h) {
        QaWorld.begin(h); unlock(h); stage(h, 3);
        var heavy = h.spawn(NpcRegistry.HEAVY.get(), HOME.offset(-4, 0, 0));
        var elite = h.spawn(NpcRegistry.ELITE.get(), HOME.offset(4, 0, 0));
        var occupied = h.spawn(NpcRegistry.HEAVY.get(), HOME.offset(0, 0, 5)); occupied.setItemSlot(EquipmentSlot.MAINHAND, Items.STICK.getDefaultInstance());
        h.runAfterDelay(50, () -> {
            h.assertTrue(heavy.hasIssuedBlade() && elite.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()), "New roles receive their existing gated kit");
            h.assertTrue(occupied.getMainHandItem().is(Items.STICK), "Never overwrite an existing held item");
            h.assertTrue(heavy.getAttributeValue(Attributes.ATTACK_DAMAGE) > NpcConfig.HEAVY.damage.get(), "Held blade increases actual melee damage");
            stage(h, 0);
            var fresh = h.spawn(NpcRegistry.HEAVY.get(), HOME.offset(7, 0, 5));
            h.runAfterDelay(50, () -> {
                h.assertTrue(heavy.hasIssuedBlade() && elite.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()) && fresh.getMainHandItem().isEmpty(), "Existing kit retained, fresh locked units unarmed");
                h.assertTrue(heavy.tier() == HumanTier.HEAVY_SOLDIER && elite.tier() == HumanTier.ELITE_SOLDIER, "Type unchanged across stage drop"); h.succeed();
            });
        });
    }
    private static <T extends net.minecraft.world.entity.Mob> void combat(GameTestHelper h, HumanTier tier, net.minecraft.world.entity.EntityType<T> targetType) {
        QaWorld.begin(h); unlock(h); stage(h, tier.stage);
        var npc = h.spawn(NpcRegistry.entityType(tier), HOME);
        var enemy = h.spawn(targetType, HOME.offset(tier.usesPrototype() ? 8 : 4, 0, 0)); enemy.setNoAi(true); enemy.setPersistenceRequired();
        float health = enemy.getHealth();
        h.succeedWhen(() -> {
            h.assertTrue(enemy.getHealth() < health || !enemy.isAlive(), "Independent AI damages " + targetType + " using " + tier);
            h.assertTrue(tier.usesBlade() ? npc.hasIssuedBlade() : npc.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()), "Correct kit issued during combat");
        });
    }
    private static void protect(GameTestHelper h) {
        QaWorld.begin(h); unlock(h); stage(h, 3);
        var heavy = h.spawn(NpcRegistry.HEAVY.get(), HOME.offset(-2, 0, 0));
        var elite = h.spawn(NpcRegistry.ELITE.get(), HOME.offset(2, 0, 0));
        var cow = h.spawn(EntityTypes.COW, HOME.offset(0, 0, 3)); cow.setNoAi(true);
        var player = QaPlayer.join(h.getLevel()); var pos = h.absolutePos(HOME.offset(0,0,-3)); player.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        float hp = cow.getHealth();
        h.runAfterDelay(100, () -> {
            h.assertFalse(heavy.canAttack(elite) || elite.canAttack(heavy) || heavy.canAttack(player) || elite.canAttack(player)
                    || heavy.canAttack(cow) || elite.canAttack(cow), "Both roles exclude players, humans and animals");
            h.assertTrue(cow.getHealth() == hp && heavy.getTarget() == null && elite.getTarget() == null, "No automatic friendly fire"); h.succeed();
        });
    }
    private static void ranged(GameTestHelper h) {
        QaWorld.begin(h); unlock(h); stage(h, 3);
        var npc = h.spawn(NpcRegistry.ELITE.get(), HOME);
        // Isolate ranged damage from melee while keeping the real acquisition/ranged goals ticking.
        npc.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var zombie = h.spawn(EntityTypes.ZOMBIE, HOME.offset(8, 0, 0)); zombie.setNoAi(true); zombie.setPersistenceRequired();
        boolean[] sawBolt = {false}; float health = zombie.getHealth();
        h.succeedWhen(() -> {
            sawBolt[0] |= !h.getLevel().getEntitiesOfClass(com.liamryan.standandhold.common.equipment.PrototypeBolt.class,
                    new AABB(h.absolutePos(HOME)).inflate(16)).isEmpty();
            h.assertTrue(sawBolt[0] && zombie.getHealth() < health && npc.distanceToSqr(zombie) > 9,
                    "Elite autonomously fires actual damaging bolts, not only melee");
        });
    }
}
