package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.config.StructureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/** Server-authoritative observation only; no SavedData access, registration, repair, or dirty calls. */
public final class OutpostPreview {
    private OutpostPreview() { }
    public static ExpansionPlan inspect(ServerPlayer player, BuildingBlockEntity post) {
        var level = player.level();
        if (!level.getServer().isSameThread() || player.isSpectator() || post.isRemoved() || post.getLevel() != level
                || !level.hasChunkAt(post.getBlockPos()) || level.getBlockEntity(post.getBlockPos()) != post
                || post.kind() != BuildingKind.COMMAND_POST
                || player.distanceToSqr(post.getBlockPos().getX() + 0.5, post.getBlockPos().getY() + 0.5, post.getBlockPos().getZ() + 0.5) > 36) {
            throw new IllegalArgumentException("Preview needs the same loaded post within 6 blocks; no spectators or remote coordinates.");
        }
        post.requireWritable();
        if (post.isMainBase()) { throw new IllegalArgumentException("Main Base expansion is not supported."); }
        if (!StructureConfig.DIMENSIONS.get().contains(level.dimension().identifier().toString())) { throw new IllegalArgumentException("Dimension not allowed by structure config."); }
        var site = post.site();
        if (!site.anchor().equals(post.getBlockPos()) || !site.dimension().equals(level.dimension().identifier().toString())) {
            throw new IllegalArgumentException("Site anchor/dimension mismatch; no repair or adoption attempted.");
        }
        ExpansionPlan.requireEligible(site, post.state().level(), level.getMinY(), level.getMaxY());
        // Original building tiles stay outside the delta and are only checked, never reconstructed.
        for (var kind : new BuildingKind[]{BuildingKind.LAB, BuildingKind.CRATE}) {
            BlockPos pos = kind == BuildingKind.LAB ? site.anchor().offset(-site.radius() + 2, 0, -site.radius() + 3)
                    : site.anchor().offset(site.radius() - 1, 0, -site.radius() + 2);
            if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !(level.getBlockEntity(pos) instanceof BuildingBlockEntity tile) || tile.kind() != kind) {
                throw new IllegalArgumentException("Original " + kind + " is missing/changed; preview changed nothing.");
            }
            tile.requireWritable();
        }
        var plan = ExpansionPlan.inspect(site, post.state().level(), level.getMinY(), level.getMaxY(), new ExpansionPlan.Terrain() {
            public boolean loaded(BlockPos pos) { return level.hasChunkAt(pos); }
            public boolean insideBorder(BlockPos pos) { return level.getWorldBorder().isWithinBounds(pos); }
            public ExpansionPlan.Sample read(BlockPos pos) {
                var state = level.getBlockState(pos);
                if (state.hasBlockEntity()) { return ExpansionPlan.Sample.TILE; }
                if (!state.getFluidState().isEmpty()) { return ExpansionPlan.Sample.WET; }
                if (state.isAir() || state.is(BlockTags.REPLACEABLE)) { return ExpansionPlan.Sample.CLEAR; }
                return (state.is(BlockTags.DIRT) || state.is(BlockTags.GRASS_BLOCKS)
                        || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.GRAVEL))
                        && state.isCollisionShapeFullBlock(level, pos)
                        ? ExpansionPlan.Sample.GROUND : ExpansionPlan.Sample.OCCUPIED;
            }
        });
        boolean[] blocked = {false}, saturated = {false}; int[] inspected = {0};
        var volumes = plan.cells().stream().map(cell -> new AABB(cell.pos())).toList();
        var center = site.center();
        var bounds = new AABB(center.getX() - 6, center.getY(), center.getZ() - 6, center.getX() + 7, center.getY() + 6, center.getZ() + 7);
        level.getEntities().get(EntityTypeTest.forClass(Entity.class), bounds, entity -> {
            if (++inspected[0] >= ExpansionPlan.MAX_ENTITIES) { saturated[0] = true; return Continuation.ABORT; }
            if (!entity.isRemoved() && !entity.isSpectator() && volumes.stream().anyMatch(box -> box.intersects(entity.getBoundingBox()))) {
                blocked[0] = true; return Continuation.ABORT;
            }
            return Continuation.CONTINUE;
        });
        if (saturated[0]) { throw new IllegalArgumentException("Preview entity inspection limit reached (128); nothing changed."); }
        if (blocked[0]) { throw new IllegalArgumentException("Entity obstructs proposed apron; nothing changed."); }
        return plan;
    }
}
