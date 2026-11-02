package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.config.StructureConfig;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/** Revision-2 player growth over the shared guarded applicator, not the fresh-site generator. */
public final class LevelTwoGrowth {
    private record Inspection(LevelTwoPlan plan, String digest) { }
    private LevelTwoGrowth() { }
    public static LevelTwoPlan inspect(ServerPlayer player, BuildingBlockEntity post) {
        OutpostPreview.requirePost(player, post); return inspect(player.level(), post);
    }
    public static LevelTwoPlan inspect(ServerLevel level, BuildingBlockEntity post) {
        return inspectDetails(level, post).plan();
    }
    private static Inspection inspectDetails(ServerLevel level, BuildingBlockEntity post) {
        var site = OutpostPreview.requirePost(level, post); post.requireGrowthWritable();
        if (post.yardOperation().isPresent()) { throw new IllegalArgumentException("Yard operation already exists; inspect status, no replay or repair."); }
        var apron = post.siteOperation().orElse(null);
        if (site.appliedRevision() == 0 && apron != null || site.appliedRevision() == 1 && (apron == null || apron.phase() != ExpansionOperation.Phase.APPLIED)) {
            throw new IllegalArgumentException("Unsealed/missing apron receipt; review saved operation before yard growth.");
        }
        var plan = LevelTwoPlan.inspect(site, post.state().level(), level.getMinY(), level.getMaxY(), new LevelTwoPlan.Terrain() {
            public boolean loaded(BlockPos pos) { return level.hasChunkAt(pos); }
            public boolean insideBorder(BlockPos pos) { return level.getWorldBorder().isWithinBounds(pos); }
            public LevelTwoPlan.Sample read(BlockPos pos) {
                var state = level.getBlockState(pos);
                boolean ground = (state.is(BlockTags.DIRT) || state.is(BlockTags.GRASS_BLOCKS) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                        || state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.GRAVEL)) && state.isCollisionShapeFullBlock(level, pos);
                return new LevelTwoPlan.Sample(NbtUtils.writeBlockState(state), ground, state.isAir() || state.is(BlockTags.REPLACEABLE), state.hasBlockEntity(), !state.getFluidState().isEmpty());
            }
        });
        var volumes = plan.changes().stream().map(cell -> cell.material() == ExpansionPlan.Material.AIR
                ? new AABB(cell.pos()).expandTowards(0, 2, 0) : new AABB(cell.pos())).toList();
        boolean[] blocked = {false}; int[] candidates = {0}; String[] obstruction = {"inspection cap reached"};
        var c = site.center();
        level.getEntities().get(EntityTypeTest.forClass(Entity.class), new AABB(c.getX()-6, c.getY(), c.getZ()-6, c.getX()+7, c.getY()+6, c.getZ()+7), entity -> {
            if (++candidates[0] >= ExpansionPlan.MAX_ENTITIES) { blocked[0] = true; return Continuation.ABORT; }
            if (!entity.isRemoved() && !entity.isSpectator() && volumes.stream().anyMatch(box -> box.intersects(entity.getBoundingBox()))) {
                blocked[0] = true; obstruction[0] = entity.getType().builtInRegistryHolder().getRegisteredName() + " at " + entity.blockPosition().toShortString(); return Continuation.ABORT;
            }
            return Continuation.CONTINUE;
        });
        if (blocked[0]) { throw new OutpostPreview.EntityObstruction("Yard blocked: " + obstruction[0] + "; move away, then Retry."); }
        // Hash untouched terrain/buildings now as well as at completion, before a receipt or block write.
        return new Inspection(plan, OutpostExpansion.yardDigest(level, post, site, plan.changes()));
    }
    private static ExpansionOperation operation(Inspection inspection) {
        var plan = inspection.plan();
        if (!StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Growth disabled by server; preview is read-only."); }
        return new ExpansionOperation(UUID.randomUUID(), plan.site().id(), plan.changes(), inspection.digest(),
                ExpansionOperation.Phase.PREPARED, 0, "", plan.site().appliedRevision(), 2);
    }
    public static ExpansionApply.Result applyNow(ServerPlayer player, BuildingBlockEntity post) {
        if (!StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Growth disabled by server."); }
        OutpostPreview.requirePost(player, post); var operation = operation(inspectDetails(player.level(), post));
        return ExpansionApply.apply(operation, OutpostExpansion.access(player, post, operation));
    }
    public static ExpansionApply.Result applyNow(ServerLevel level, BuildingBlockEntity post) {
        if (!StructureConfig.EXPANSION.get()) { throw new IllegalArgumentException("Growth disabled by server."); }
        var operation = operation(inspectDetails(level, post));
        return ExpansionApply.apply(operation, OutpostExpansion.access(level, post, operation));
    }
}
