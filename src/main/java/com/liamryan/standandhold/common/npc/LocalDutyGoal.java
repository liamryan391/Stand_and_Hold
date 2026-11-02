package com.liamryan.standandhold.common.npc;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** Two local waypoints or one reserved deck position, using vanilla navigation. */
public final class LocalDutyGoal extends Goal {
    private final HumanNpc npc;
    private BuildingBlockEntity post;
    private BlockPos destination;
    private boolean watch;
    private long nextResolve, nextPath, requests;
    private int failures;
    private double lastDistance = Double.MAX_VALUE;
    LocalDutyGoal(HumanNpc npc) { this.npc = npc; setFlags(java.util.EnumSet.of(Flag.MOVE)); }
    public BlockPos destination() { return destination; }
    public boolean watch() { return watch && destination != null; }
    public long requests() { return requests; }
    @Override public boolean canUse() {
        if (!(npc.level() instanceof ServerLevel level) || npc.getTarget() != null || npc.hasResponseOrder() || npc.commandPost().isEmpty()) { return false; }
        var home = npc.commandPost().orElseThrow();
        if (!home.dimension().equals(level.dimension().identifier()) || !level.hasChunkAt(home.pos())) { return false; }
        if (post != null && watch && !npc.getUUID().equals(post.duty().watch())) {
            destination = null; post = null; watch = false; nextResolve = 0;
        }
        if (level.getGameTime() >= nextResolve) {
            nextResolve = level.getGameTime() + DefenderDuty.RESOLVE_TICKS;
            try {
                post = DefenderDuties.post(level, npc);
                if (post == null || post.duty().mode() != DefenderDuty.Mode.PATROL) { destination = null; return false; }
                watch = DefenderDuties.claimWatch(level, post, npc);
                var next = watch ? DefenderDuties.watchPosition(post) : DefenderDuties.waypoint(post, npc.localWaypoint());
                if (!java.util.Objects.equals(next, destination)) {
                    destination = next; failures = 0; lastDistance = Double.MAX_VALUE; nextPath = level.getGameTime();
                }
            } catch (IllegalArgumentException | IllegalStateException unavailable) { post = null; destination = null; return false; }
        }
        return post != null && !post.isRemoved() && post.dutySupported() && post.duty().mode() == DefenderDuty.Mode.PATROL;
    }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        if (destination == null || !(npc.level() instanceof ServerLevel level)) { return; }
        double distance = destination.distToCenterSqr(npc.position());
        if (distance < 1.0) {
            if (!npc.getNavigation().isDone()) { npc.getNavigation().stop(); }
            failures = 0;
            if (!watch) { npc.localWaypoint(1 - npc.localWaypoint()); destination = null; nextResolve = level.getGameTime() + 40; }
            return;
        }
        long now = level.getGameTime();
        if (now < nextPath) { return; }
        nextPath = now + DefenderDuty.PATH_TICKS;
        if (!npc.getNavigation().isDone() && distance < lastDistance - 0.25) { lastDistance = distance; failures = 0; return; }
        if (safeDestination(level, npc, destination)) {
            requests++;
            var path = npc.getNavigation().createPath(destination, 0);
            if (path != null && path.canReach()) { npc.getNavigation().moveTo(path, 0.8); }
            else { npc.getNavigation().stop(); }
        } else { npc.getNavigation().stop(); }
        lastDistance = distance;
        if (++failures >= DefenderDuty.MAX_ATTEMPTS) { nextPath = now + DefenderDuty.BACKOFF_TICKS; failures = 0; }
    }
    public static boolean safeDestination(ServerLevel level, HumanNpc npc, BlockPos pos) {
        return npc.isWithinHome(pos) && level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 2, 1))
                && level.getWorldBorder().isWithinBounds(pos) && level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos.below()).isEmpty() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && level.noCollision(npc, npc.getBoundingBox().move(pos.getX() + .5 - npc.getX(), pos.getY() - npc.getY(), pos.getZ() + .5 - npc.getZ()));
    }
    @Override public void stop() { npc.getNavigation().stop(); nextResolve = 0; }
}
