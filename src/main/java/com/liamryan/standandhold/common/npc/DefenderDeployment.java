package com.liamryan.standandhold.common.npc;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.NpcConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;

/** Called only by a loaded post's interval; all queries are bounded and never request chunks. */
public final class DefenderDeployment {
    private DefenderDeployment() { }
    public static HumanNpc attempt(ServerLevel level, BuildingBlockEntity post) {
        if (!NpcConfig.DEPLOY.get() || NpcConfig.POST_CAP.get() == 0) { return null; }
        var data = HumanWorldData.get(level.getServer());
        int stage = ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level();
        var tier = HumanTier.unlocked(stage, NpcConfig.SURVIVOR.stage.get(), NpcConfig.ARMY.stage.get());
        if (tier == null) { return null; }
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        int reserved = data.defenderCount(home.key());
        if (reserved >= NpcConfig.POST_CAP.get()) { return null; }
        int[] nearby = {0};
        level.getEntities().get(EntityTypeTest.forClass(HumanNpc.class), new AABB(home.pos()).inflate(NpcConfig.AREA_RADIUS.get()), npc -> {
            if (npc.isAlive()) { nearby[0]++; }
            return nearby[0] >= NpcConfig.AREA_CAP.get() ? Continuation.ABORT : Continuation.CONTINUE;
        });
        if (!HumanTier.hasRoom(reserved, NpcConfig.POST_CAP.get(), nearby[0], NpcConfig.AREA_CAP.get())) { return null; }
        var type = tier == HumanTier.ARMY_RIFLEMAN ? NpcRegistry.ARMY.get() : NpcRegistry.SURVIVOR.get();
        var npc = type.create(level, EntitySpawnReason.REINFORCEMENT);
        if (npc == null) { return null; }
        int radius = NpcConfig.SPAWN_RADIUS.get();
        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = level.getRandom().nextInt(radius * 2 + 1) - radius;
            int dz = level.getRandom().nextInt(radius * 2 + 1) - radius;
            if (dx == 0 && dz == 0) { continue; }
            for (int dy : new int[] {0, 1, -1, 2, -2}) {
                BlockPos pos = home.pos().offset(dx, dy, dz);
                if (!level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 2, 1))
                        || pos.getY() <= level.getMinY() || pos.getY() + 2 >= level.getMaxY()
                        || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                        || !level.getFluidState(pos.below()).isEmpty()) { continue; }
                npc.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (!level.getWorldBorder().isWithinBounds(npc.getBoundingBox()) || !level.noCollision(npc)
                        || !level.isUnobstructed(npc) || WalkNodeEvaluator.getPathTypeStatic(npc, pos) != PathType.WALKABLE) { continue; }
                npc.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.REINFORCEMENT, null);
                if (npc.isSpawnCancelled() || npc.isRemoved()) { return null; }
                npc.assignPost(home);
                if (!data.reserveDefender(home.key(), npc.getUUID(), NpcConfig.POST_CAP.get())) { return null; }
                boolean added = false;
                try { added = level.addFreshEntity(npc); }
                finally { if (!added) { data.releaseDefender(home.key(), npc.getUUID()); } }
                return added ? npc : null;
            }
        }
        return null;
    }
}
