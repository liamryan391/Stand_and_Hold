package com.liamryan.standandhold.common.support;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.foundation.FoundationAmounts;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.threat.ThreatResponse;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.ResponseConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;

/** Explicit server action, no scheduler or additional entity/quota budget. */
public final class SupportCallIn {
    public static final int RANGE = 6;
    private SupportCallIn() { }
    public static BuildingBlockEntity lookedAt(ServerPlayer player) {
        var level = player.level(); var eye = player.getEyePosition(); var end = eye.add(player.getViewVector(1).scale(RANGE));
        BlockPos a = BlockPos.containing(eye), b = BlockPos.containing(end);
        if (!level.hasChunksAt(new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ())),
                new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ())))) {
            throw new IllegalArgumentException("Look at a loaded Field Command Post within 6 blocks; no remote chunk loading.");
        }
        if (player.pick(RANGE, 1, false) instanceof BlockHitResult hit && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && level.hasChunkAt(hit.getBlockPos())
                && level.getBlockEntity(hit.getBlockPos()) instanceof BuildingBlockEntity post && post.kind() == BuildingKind.COMMAND_POST) { return post; }
        throw new IllegalArgumentException("Look directly at a Field Command Post within 6 blocks.");
    }
    public static ThreatResponse.Deployment request(ServerPlayer player, BuildingBlockEntity post) {
        var level = player.level(); var data = HumanWorldData.get(level.getServer());
        data.requireSupportedVersion(); post.requireWritable();
        if (!ResponseConfig.SUPPORT.get() || player.isSpectator()) { throw new IllegalArgumentException("Player support call-ins are disabled or unavailable to spectators."); }
        if (post.kind() != BuildingKind.COMMAND_POST || post.getLevel() != level || post.isRemoved()
                || !level.hasChunkAt(post.getBlockPos()) || level.getBlockEntity(post.getBlockPos()) != post
                || player.distanceToSqr(post.getBlockPos().getX() + 0.5, post.getBlockPos().getY() + 0.5, post.getBlockPos().getZ() + 0.5) > RANGE * RANGE
                || !post.operational(data)) { throw new IllegalArgumentException("Command Post is too far away, missing, unloaded or dormant."); }
        if (ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level() < ResponseConfig.SUPPORT_STAGE.get()) {
            throw new IllegalArgumentException("Support requires human Stage " + ResponseConfig.SUPPORT_STAGE.get() + ".");
        }
        String research = ResponseConfig.SUPPORT_RESEARCH.get();
        if (!research.isEmpty()) {
            FoundationCatalog.requireId(research);
            if (!data.isResearchComplete(research)) { throw new IllegalArgumentException("Support requires research " + research + "."); }
        }
        int cost = ResponseConfig.SUPPORT_COST.get();
        data.defenderCount(new BuildingPosition(level.dimension().identifier(), post.getBlockPos()).key());
        ThreatResponse.state(level, post); // Validate saved response before reserving supplies.
        data.setSupplies(FoundationAmounts.debit(data.supplies(), cost));
        // Reserve before the spawn hook. An unknown exception retains this debit rather than
        // refunding a potentially spawned unit; known refusals return a structured false result.
        ThreatResponse.Deployment result;
        try { result = ThreatResponse.deployReinforcement(level, post); }
        catch (RuntimeException failure) {
            com.mojang.logging.LogUtils.getLogger().error("Support deployment failed at {}; {} reserved supplies retained to avoid duplicating a possible spawned unit", post.getBlockPos(), cost, failure);
            throw new IllegalStateException("Support failed unexpectedly after reserving " + cost + " supplies; ask an operator to inspect the server log before retrying.", failure);
        }
        if (!result.success()) {
            data.setSupplies(FoundationAmounts.credit(data.supplies(), cost));
            return result;
        }
        return new ThreatResponse.Deployment(true, result.message() + " Spent " + cost + " global supplies.");
    }
}
