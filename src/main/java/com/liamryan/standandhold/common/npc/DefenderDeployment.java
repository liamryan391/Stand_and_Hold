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

/** Shared by loaded-post intervals, events and paid call-ins. Never requests chunks. */
public final class DefenderDeployment {
    private DefenderDeployment() { }
    public record Status(int assigned, int capacity, int reason) { }
    public static HumanTier eligibleTier(ServerLevel level) {
        var data = HumanWorldData.get(level.getServer());
        data.requireSupportedVersion();
        var catalog = com.liamryan.standandhold.common.foundation.FoundationResources.get(level.getServer());
        int stage = ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level();
        return HumanTier.unlocked(stage, tier -> NpcConfig.stats(tier).stage.get(), tier -> {
            var research = NpcConfig.stats(tier).research;
            if (research == null) { return true; }
            String id = research.get();
            if (id == null || id.isBlank() || net.minecraft.resources.Identifier.tryParse(id) == null) { return false; }
            return catalog.research().containsKey(id) && data.isResearchComplete(id);
        });
    }
    // Stable menu codes: 0 slots, 1 disabled, 2 dormant, 3 stage locked, 4 full, 5 unavailable.
    public static Status status(ServerLevel level, BuildingBlockEntity post) {
        post.requireWritable();
        var data = HumanWorldData.get(level.getServer());
        data.requireSupportedVersion();
        int cap = NpcConfig.capacity(post.state().level());
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        int assigned = data.defenderCount(home.key());
        int reason = !NpcConfig.DEPLOY.get() || cap == 0 ? 1 : !post.operational(data) ? 2
                : eligibleTier(level) == null ? 3
                : assigned >= cap ? 4 : 0;
        return new Status(assigned, cap, reason);
    }
    public static HumanNpc attempt(ServerLevel level, BuildingBlockEntity post) {
        if (!level.getServer().isSameThread() || post.getLevel() != level || post.isRemoved()
                || !level.hasChunkAt(post.getBlockPos()) || level.getBlockEntity(post.getBlockPos()) != post
                || post.kind() != com.liamryan.standandhold.common.building.BuildingKind.COMMAND_POST) { return null; }
        var status = status(level, post);
        if (status.reason() != 0) { return null; }
        var data = HumanWorldData.get(level.getServer());
        var tier = eligibleTier(level);
        if (tier == null) { return null; }
        var home = new BuildingPosition(level.dimension().identifier(), post.getBlockPos());
        int reserved = status.assigned(), capacity = status.capacity();
        int[] nearby = {0};
        level.getEntities().get(EntityTypeTest.forClass(HumanNpc.class), new AABB(home.pos()).inflate(NpcConfig.AREA_RADIUS.get()), npc -> {
            if (npc.isAlive()) { nearby[0]++; }
            return nearby[0] >= NpcConfig.AREA_CAP.get() ? Continuation.ABORT : Continuation.CONTINUE;
        });
        if (!HumanTier.hasRoom(reserved, capacity, nearby[0], NpcConfig.AREA_CAP.get())) { return null; }
        var type = NpcRegistry.entityType(tier);
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
                if (!data.reserveDefender(home.key(), npc.getUUID(), capacity)) { return null; }
                boolean added = false;
                try { added = level.addFreshEntity(npc); }
                finally { if (!added) { data.releaseDefender(home.key(), npc.getUUID()); } }
                return added ? npc : null;
            }
        }
        return null;
    }
}
