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
    private static final List<HumanNpc> seeded = new ArrayList<>();
    static void seed(ServerLevel level, Properties receipt) {
        level.setBlockAndUpdate(POST, BuildingRegistry.COMMAND_POST.get().defaultBlockState());
        var post = (BuildingBlockEntity) level.getBlockEntity(POST);
        post.change(new BuildingState(2, 7, 1, "", 0));
        var home = new BuildingPosition(level.dimension().identifier(), POST);
        HumanWorldData.get(level.getServer()).registerCommandPost(home);
        for (var tier : List.of(HumanTier.HEAVY_SOLDIER, HumanTier.ELITE_SOLDIER)) {
            var npc = NpcRegistry.entityType(tier).create(level, EntitySpawnReason.COMMAND);
            check(npc != null, "New tier factory");
            npc.snapTo(53.5 + seeded.size()*5, 64, 11.5, 0, 0); npc.assignPost(home);
            npc.setHealth(npc.getMaxHealth() - 7);
            check(HumanWorldData.get(level.getServer()).reserveDefender(home.key(), npc.getUUID(), 5), "New tier reserved normally");
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
    static void verify(ServerLevel level, Properties receipt) {
        var home = new BuildingPosition(level.dimension().identifier(), POST);
        var data = HumanWorldData.get(level.getServer());
        check(data.defenderCount(home.key()) == 2, "Mixed tier roster persisted at stage zero");
        for (var tier : List.of(HumanTier.HEAVY_SOLDIER, HumanTier.ELITE_SOLDIER)) {
            var entity = level.getEntity(UUID.fromString(receipt.getProperty(tier.id)));
            check(entity instanceof HumanNpc, "Same saved tier UUID loaded"); var npc = (HumanNpc) entity;
            check(npc.tier() == tier && npc.commandPost().orElseThrow().equals(home), "Type/home never demoted on reload");
            check(npc.getHealth() == Float.parseFloat(receipt.getProperty(tier.id + "Health")), "Damaged health never healed on reload");
            check(tier.usesBlade() ? npc.hasIssuedBlade() : npc.getMainHandItem().is(EquipmentRegistry.PROTOTYPE.get()), "Retained physical kit across real stage-drop restart");
        }
        var post = (BuildingBlockEntity) level.getBlockEntity(POST);
        check(post.state().equals(new BuildingState(2, 7, 1, "", 0)), "New tier post storage unchanged");
    }
    private static void check(boolean ok, String message) { if (!ok) { throw new IllegalStateException(message); } }
}
