package com.liamryan.standandhold.qa;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.npc.*;
import com.liamryan.standandhold.common.world.HumanWorldData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

/** Reuses the existing real yard: no new site count, population path or test-only production hook. */
final class RestartDutyFixture {
    static void seed(ServerLevel level, Properties receipt) {
        var pos = BlockPos.of(Long.parseLong(receipt.getProperty("yard.0.post")));
        var post = (BuildingBlockEntity) level.getBlockEntity(pos); var home = new BuildingPosition(level.dimension().identifier(), pos);
        var npc = NpcRegistry.SURVIVOR.get().create(level, EntitySpawnReason.COMMAND); check(npc != null, "Duty actor factory");
        npc.assignPost(home); npc.localWaypoint(1); npc.setHealth(17); npc.setItemSlot(EquipmentSlot.MAINHAND, Items.STICK.getDefaultInstance()); npc.setNoAi(true);
        var watch = DefenderDuties.watchPosition(post); check(watch != null, "Existing applied yard has a real watch position");
        npc.snapTo(watch.getX() + .5, watch.getY(), watch.getZ() + .5, 0, 0);
        var data = HumanWorldData.get(level.getServer()); check(data.reserveDefender(home.key(), npc.getUUID(), 5) && level.addFreshEntity(npc), "One existing assigned identity before save");
        DefenderDuties.setMode(level, post, DefenderDuty.Mode.PATROL); check(DefenderDuties.claimWatch(level, post, npc), "Watch owned before real shutdown");
        receipt.setProperty("duty.npc", npc.getUUID().toString()); receipt.setProperty("duty.post", Long.toString(pos.asLong()));
    }
    static void verify(ServerLevel level, Properties receipt) {
        var post = (BuildingBlockEntity) level.getBlockEntity(BlockPos.of(Long.parseLong(receipt.getProperty("duty.post")))); var id = UUID.fromString(receipt.getProperty("duty.npc"));
        check(post != null && post.duty().mode() == DefenderDuty.Mode.PATROL && id.equals(post.duty().watch()), "Same saved local policy and single watch owner");
        check(level.getEntity(id) instanceof HumanNpc, "Same physical NPC UUID reloaded"); var npc = (HumanNpc) level.getEntity(id);
        var key = new BuildingPosition(level.dimension().identifier(), post.getBlockPos()).key();
        check(npc.commandPost().map(p -> p.key().equals(key)).orElse(false) && npc.localWaypoint() == 1 && npc.getHealth() == 17 && npc.getMainHandItem().is(Items.STICK)
                && HumanWorldData.get(level.getServer()).defenderIds(key).equals(Set.of(id)), "Home/waypoint/health/type/gear/quota retained, not reissued or duplicated");
        check(npc.blockPosition().equals(DefenderDuties.watchPosition(post)), "Physical watch pose survives dedicated JVM restart");
    }
    private static void check(boolean condition, String message) { if (!condition) { throw new IllegalStateException(message); } }
}
