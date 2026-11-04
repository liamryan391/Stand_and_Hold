package com.liamryan.standandhold.common.npc;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.common.structure.ExpansionOperation;
import com.liamryan.standandhold.common.world.HumanWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Post/UUID lookups only. Missing loaded entities never release a watch reservation. */
public final class DefenderDuties {
    private DefenderDuties() { }
    public static BuildingBlockEntity post(ServerLevel level, HumanNpc npc) {
        var home = npc.commandPost().orElse(null);
        if (home == null || !home.dimension().equals(level.dimension().identifier()) || !level.hasChunkAt(home.pos())) { return null; }
        if (!(level.getBlockEntity(home.pos()) instanceof BuildingBlockEntity post) || post.kind() != BuildingKind.COMMAND_POST
                || post.isRemoved() || !post.dutySupported()) { return null; }
        post.requireWritable(); var data = HumanWorldData.get(level.getServer());
        if (!data.hasDefender(home.key(), npc.getUUID()) || !post.operational(data)) { return null; }
        return post;
    }
    public static void setMode(ServerLevel level, BuildingBlockEntity post, DefenderDuty.Mode mode) {
        if (!level.getServer().isSameThread() || post.getLevel() != level || post.isRemoved()
                || !level.hasChunkAt(post.getBlockPos()) || level.getBlockEntity(post.getBlockPos()) != post) {
            throw new IllegalStateException("Loaded local Command Post required.");
        }
        post.changeDuty(post.duty().mode(mode));
    }
    public static BlockPos watchPosition(BuildingBlockEntity post) {
        try {
            var site = post.site();
            boolean supported = site.appliedRevision() == 2 || site.appliedRevision() == 3
                    && post.deckOperation().orElseThrow().phase() == ExpansionOperation.Phase.APPLIED;
            return supported && post.yardOperation().orElseThrow().phase() == ExpansionOperation.Phase.APPLIED
                    ? site.center().offset(-4, 3, 0) : null;
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException invalid) { return null; }
    }
    public static boolean claimWatch(ServerLevel level, BuildingBlockEntity post, HumanNpc npc) {
        var data = HumanWorldData.get(level.getServer());
        var home = npc.commandPost().orElseThrow();
        if (post(level, npc) != post || post.duty().mode() != DefenderDuty.Mode.PATROL || watchPosition(post) == null) { return false; }
        var owner = post.duty().watch();
        if (owner != null && !data.hasDefender(home.key(), owner)) { owner = null; }
        if (owner == null) { post.changeDuty(post.duty().watch(npc.getUUID())); return true; }
        return owner.equals(npc.getUUID());
    }
    // Pure menu status: no entity scan, slot claim, dirty flag or chunk request.
    public static int watchStatus(ServerLevel level, BuildingBlockEntity post) {
        if (!post.dutySupported()) { return 4; }
        if (post.duty().mode() == DefenderDuty.Mode.GUARD) { return 0; }
        if (!post.operational(HumanWorldData.get(level.getServer()))) { return 3; }
        if (watchPosition(post) == null) { return 1; }
        return post.duty().watch() == null ? 2 : 5;
    }
    public static BlockPos waypoint(BuildingBlockEntity post, int index) {
        int east = 2;
        try {
            if (post.site().appliedRevision() == 3 && post.deckOperation().orElseThrow().phase() == ExpansionOperation.Phase.APPLIED) { east = 4; }
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException unsupported) { /* Keep ordinary/older posts' original targets. */ }
        return post.getBlockPos().offset(index == 0 ? -2 : east, 0, 3);
    }
}
