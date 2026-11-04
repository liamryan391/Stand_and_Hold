package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/** Observation only; deliberately not connected to the growth applicator or automatic cadence. */
public final class LevelThreePreview {
    private LevelThreePreview() { }
    public static LevelThreePlan inspect(ServerPlayer player, BuildingBlockEntity post) {
        OutpostPreview.requirePost(player, post); return inspect(player.level(), post);
    }
    public static LevelThreePlan inspect(ServerLevel level, BuildingBlockEntity post) {
        var site = OutpostPreview.requirePost(level, post); post.requireGrowthWritable();
        var yard = post.yardOperation().orElseThrow(() -> new IllegalArgumentException("Level-3 needs an APPLIED revision-2 yard receipt."));
        var apron = post.siteOperation().orElse(null);
        if (site.appliedRevision() != 2 || yard.phase() != ExpansionOperation.Phase.APPLIED
                || yard.sourceRevision() == 0 && apron != null
                || yard.sourceRevision() == 1 && (apron == null || apron.phase() != ExpansionOperation.Phase.APPLIED)) {
            throw new IllegalArgumentException("Level-3 needs consistent APPLIED source receipts; no replay or repair.");
        }
        var plan = LevelThreePlan.inspect(site, post.state().level(), level.getMinY(), level.getMaxY(), new LevelTwoPlan.Terrain() {
            public boolean loaded(BlockPos pos) { return level.hasChunkAt(pos); }
            public boolean insideBorder(BlockPos pos) { return level.getWorldBorder().isWithinBounds(pos); }
            public LevelTwoPlan.Sample read(BlockPos pos) {
                var state = level.getBlockState(pos);
                boolean ground = (state.is(BlockTags.DIRT) || state.is(BlockTags.GRASS_BLOCKS) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                        || state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.GRAVEL)) && state.isCollisionShapeFullBlock(level, pos);
                return new LevelTwoPlan.Sample(NbtUtils.writeBlockState(state), ground, state.isAir(), state.hasBlockEntity(), !state.getFluidState().isEmpty());
            }
        });
        var volumes = plan.changes().stream().map(cell -> cell.before().getStringOr("id", "").equals("minecraft:smooth_stone_slab")
                ? new AABB(cell.pos()).expandTowards(0, 2, 0) : new AABB(cell.pos())).toList();
        var c = site.center(); int[] candidates = {0}; String[] blocked = {null};
        level.getEntities().get(EntityTypeTest.forClass(Entity.class), new AABB(c.getX()-6, c.getY(), c.getZ()-6, c.getX()+7, c.getY()+8, c.getZ()+7), entity -> {
            if (++candidates[0] >= ExpansionPlan.MAX_ENTITIES) { blocked[0] = "entity inspection limit (128)"; return Continuation.ABORT; }
            if (!entity.isRemoved() && !entity.isSpectator() && volumes.stream().anyMatch(box -> box.intersects(entity.getBoundingBox()))) {
                blocked[0] = entity.getType().builtInRegistryHolder().getRegisteredName() + " at " + entity.blockPosition().toShortString(); return Continuation.ABORT;
            }
            return Continuation.CONTINUE;
        });
        if (blocked[0] != null) { throw new OutpostPreview.EntityObstruction("Level-3 preview blocked: " + blocked[0] + "; no actor moved, nothing changed."); }
        return plan;
    }
    public static void describe(ServerPlayer player, BuildingBlockEntity post) {
        var plan = inspect(player, post); var c = plan.site().center();
        for (String line : new String[]{"Level-3 upper deck / READ ONLY / revision 2 -> 3 / " + plan.changes().size() + " planned cells.",
                "5x4 deck, two-wide stairs; 9 roof slabs replaced, no old wall removals.",
                "Envelope x:" + (c.getX()-6) + ".." + (c.getX()+6) + " z:" + (c.getZ()-6) + ".." + (c.getZ()+6) + " y:" + (c.getY()-1) + ".." + (c.getY()+7),
                "Buildings, storage, units and modes stay; no writes, cost, rewards or receipt. Placement is not enabled."}) {
            player.sendSystemMessage(Component.literal(line));
        }
    }
}
