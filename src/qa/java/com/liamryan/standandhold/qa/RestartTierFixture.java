package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.equipment.EquipmentRegistry;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;

/** Separate saved roster, real type IDs/equipment and damaged health across two JVMs. */
final class RestartTierFixture {
    private static final BlockPos POST = new BlockPos(56, 64, 8);
    private static final List<HumanTier> TIERS = List.of(HumanTier.HEAVY_SOLDIER, HumanTier.ELITE_SOLDIER,
            HumanTier.SUPER_ELITE, HumanTier.SPECIAL_DIVISION);
    private static final List<HumanNpc> seeded = new ArrayList<>();
    static void seed(ServerLevel level, BlockPos base, Properties receipt) {
        level.setBlockAndUpdate(POST, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        var post = (BuildingBlockEntity) level.getBlockEntity(POST);
        post.change(new BuildingState(2, 7, 1, "", 0));
        var home = new BuildingPosition(level.dimension().identifier(), POST);
        HumanWorldData.get(level.getServer()).registerCommandPost(home);
        for (var tier : TIERS) {
            boolean division = tier == HumanTier.SPECIAL_DIVISION;
            var assignment = division ? new BuildingPosition(level.dimension().identifier(), base) : home;
            var npc = NpcRegistry.entityType(tier).create(level, EntitySpawnReason.COMMAND);
            check(npc != null, "New tier factory");
            npc.snapTo(division ? base.getX()+1.5 : 53.5+seeded.size()*3.5,
                    64, division ? base.getZ()-2.5 : 11.5, 0, 0);
            npc.assignPost(assignment);
            npc.setHealth(npc.getMaxHealth() - 7);
            check(HumanWorldData.get(level.getServer()).reserveDefender(assignment.key(), npc.getUUID(), 5, division), "New tier reserved normally");
            check(level.addFreshEntity(npc), "Real tier added before restart"); seeded.add(npc);
            receipt.setProperty(tier.id, npc.getUUID().toString());
            receipt.setProperty(tier.id + "Health", Float.toString(npc.getHealth()));
        }
    }
    static void freeze() {
        for (var npc : seeded) {
            check(npc.tier().usesBlade() ? npc.hasIssuedBlade() : npc.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()), "Real automatic kit issue before restart");
            npc.setNoAi(true);
        }
    }
    static void verify(ServerLevel level, BlockPos base, Properties receipt) {
        var home = new BuildingPosition(level.dimension().identifier(), POST);
        var baseHome = new BuildingPosition(level.dimension().identifier(), base);
        var data = HumanWorldData.get(level.getServer());
        check(data.defenderCount(home.key()) == 3 && data.hasDivisionDefender(baseHome.key()), "Mixed roster and Division slot persisted at stage zero");
        check(!data.reserveDefender(baseHome.key(), UUID.randomUUID(), 5, true), "Second Division slot refused after restart");
        for (var tier : TIERS) {
            var id = UUID.fromString(receipt.getProperty(tier.id));
            var assignment = tier == HumanTier.SPECIAL_DIVISION ? baseHome : home;
            var entity = level.getEntity(id);
            check(entity instanceof HumanNpc, "Same saved tier UUID loaded"); var npc = (HumanNpc) entity;
            check(npc.tier() == tier && npc.commandPost().orElseThrow().equals(assignment)
                    && data.hasDefender(assignment.key(), id), "Type/home/reservation never demoted on reload");
            check(npc.getHealth() == Float.parseFloat(receipt.getProperty(tier.id + "Health")), "Damaged health never healed on reload");
            check(tier.usesBlade() ? npc.hasIssuedBlade() : npc.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()), "Retained physical kit across real stage-drop restart");
        }
        var post = (BuildingBlockEntity) level.getBlockEntity(POST);
        check(post.state().equals(new BuildingState(2, 7, 1, "", 0)), "New tier post storage unchanged");
    }
    private static void check(boolean ok, String message) { if (!ok) { throw new IllegalStateException(message); } }
}
