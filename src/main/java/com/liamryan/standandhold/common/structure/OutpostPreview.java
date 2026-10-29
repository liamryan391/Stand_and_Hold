package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.*;
import com.liamryan.standandhold.config.StructureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/** Server-authoritative observation only; no SavedData access, registration, repair, or dirty calls. */
public final class OutpostPreview {
    private OutpostPreview() { }
    public static final class EntityObstruction extends IllegalArgumentException {
        public EntityObstruction(String message) { super(message); }
    }
    public record Inspection(ExpansionPlan plan, java.util.List<ExpansionOperation.Change> changes) { }
    public static SiteProvenance requirePost(ServerPlayer player, BuildingBlockEntity post) {
        var level = player.level();
        if (player.isRemoved() || !player.isAlive() || player.isSpectator()
                || player.distanceToSqr(post.getBlockPos().getX() + 0.5, post.getBlockPos().getY() + 0.5, post.getBlockPos().getZ() + 0.5) > 36) {
            throw new IllegalArgumentException("Preview needs the same loaded post within 6 blocks; no spectators or remote coordinates.");
        }
        return requirePost(level, post);
    }
    public static SiteProvenance requirePost(ServerLevel level, BuildingBlockEntity post) {
        if (!level.getServer().isSameThread() || post.isRemoved() || post.getLevel() != level
                || !level.hasChunkAt(post.getBlockPos()) || level.getBlockEntity(post.getBlockPos()) != post
                || post.kind() != BuildingKind.COMMAND_POST) {
            throw new IllegalArgumentException("Preview needs the same loaded post within 6 blocks; no spectators or remote coordinates.");
        }
        post.requireWritable();
        if (post.isMainBase()) { throw new IllegalArgumentException("Main Base expansion is not supported."); }
        if (!StructureConfig.DIMENSIONS.get().contains(level.dimension().identifier().toString())) { throw new IllegalArgumentException("Dimension not allowed by structure config."); }
        var site = post.site();
        if (!site.anchor().equals(post.getBlockPos()) || !site.dimension().equals(level.dimension().identifier().toString())) {
            throw new IllegalArgumentException("Site anchor/dimension mismatch; no repair or adoption attempted.");
        }
        if (site.radius() != 4 || site.appliedRevision() > 1) { throw new IllegalArgumentException("Unsupported site radius/applied revision."); }
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
        return site;
    }
    public static ExpansionPlan inspect(ServerPlayer player, BuildingBlockEntity post) { return inspectDetails(player, post).plan(); }
    public static Inspection inspectDetails(ServerPlayer player, BuildingBlockEntity post) {
        requirePost(player, post); return inspectDetails(player.level(), post);
    }
    public static Inspection inspectDetails(ServerLevel level, BuildingBlockEntity post) {
        var site = requirePost(level, post);
        ExpansionPlan.requireEligible(site, post.state().level(), level.getMinY(), level.getMaxY());
        var pending = post.siteOperation().orElse(null);
        if (pending != null && pending.phase() != ExpansionOperation.Phase.PREPARED) { throw new IllegalArgumentException("Existing expansion operation needs operator review; use /standandhold outpost status."); }
        var states = new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
        var plan = ExpansionPlan.inspect(site, post.state().level(), level.getMinY(), level.getMaxY(), new ExpansionPlan.Terrain() {
            public boolean loaded(BlockPos pos) { return level.hasChunkAt(pos); }
            public boolean insideBorder(BlockPos pos) { return level.getWorldBorder().isWithinBounds(pos); }
            public ExpansionPlan.Sample read(BlockPos pos) {
                var state = level.getBlockState(pos);
                states.put(pos, state);
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
        if (saturated[0]) { throw new EntityObstruction("Preview entity inspection limit reached (128); nothing changed."); }
        if (blocked[0]) { throw new EntityObstruction("Entity obstructs proposed apron; nothing changed."); }
        var changes = plan.cells().stream().map(cell -> new ExpansionOperation.Change(cell.pos(), net.minecraft.nbt.NbtUtils.writeBlockState(states.get(cell.pos())), cell.material())).toList();
        return new Inspection(plan, changes);
    }
}
