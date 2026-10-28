package com.liamryan.standandhold.common.structure;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingKind;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingRegistry;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.BuildingConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class StructurePlacement {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private StructurePlacement() { }
    public record Result(boolean success, String message, BlockPos commandPost) { }

    /** Runs either inside the current feature chunk or on the server thread for an already-loaded debug chunk. */
    public static Result place(WorldGenLevel world, StructureKind kind, int chunkX, int chunkZ) {
        Map<BlockPos, BlockState> before = new LinkedHashMap<>();
        try {
            if (!StructureConfig.DIMENSIONS.get().contains(world.getLevel().dimension().identifier().toString())) {
                throw new IllegalArgumentException("Dimension not allowed by structure config.");
            }
            if (!world.hasChunk(chunkX, chunkZ)) { throw new IllegalArgumentException("Target chunk is not loaded/available; no chunk was requested."); }
            int radius = kind == StructureKind.CHECKPOINT ? StructureConfig.CHECKPOINT_RADIUS.get() : StructureConfig.MAIN_BASE_RADIUS.get();
            StructurePlan plan = StructurePlan.plan(kind, chunkX, chunkZ, radius, StructureConfig.SLOPE.get(), world.getMinY(), world.getMaxY(),
                    new StructurePlan.Terrain() {
                        public int surface(int x, int z) { return world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1; }
                        public boolean ground(BlockPos pos) {
                            BlockState state = world.getBlockState(pos);
                            return !state.hasBlockEntity() && state.getFluidState().isEmpty() && state.isCollisionShapeFullBlock(world, pos)
                                    && (state.is(BlockTags.DIRT) || state.is(BlockTags.GRASS_BLOCKS) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.SAND)
                                    || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.GRAVEL));
                        }
                        public boolean clear(BlockPos pos) {
                            BlockState state = world.getBlockState(pos);
                            return !state.hasBlockEntity() && state.getFluidState().isEmpty() && (state.isAir() || state.is(BlockTags.REPLACEABLE));
                        }
                        public boolean writable(BlockPos pos) {
                            return pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ
                                    && world.getWorldBorder().isWithinBounds(pos) && world.ensureCanWrite(pos);
                        }
                    });
            var position = new BuildingPosition(world.getLevel().dimension().identifier(), plan.commandPost());
            HumanWorldData data = world instanceof ServerLevel server ? HumanWorldData.get(server.getServer()) : null;
            if (data != null) { data.checkSiteCapacity(position, kind == StructureKind.MAIN_BASE); }
            for (var cell : plan.cells()) {
                BlockState state = block(cell.material());
                BlockState old = world.getBlockState(cell.pos());
                if (old.equals(state)) { continue; }
                before.put(cell.pos(), old);
                if (!world.setBlock(cell.pos(), state, FLAGS) && !world.getBlockState(cell.pos()).equals(state)) {
                    throw new IllegalStateException("Block placement refused; restoring footprint.");
                }
            }
            if (!(world.getBlockEntity(plan.commandPost()) instanceof BuildingBlockEntity post) || post.kind() != BuildingKind.COMMAND_POST) {
                throw new IllegalStateException("Command Post tile missing; restoring footprint.");
            }
            for (var cell : plan.cells()) {
                if (cell.material() == StructurePlan.Material.CRATE || cell.material() == StructurePlan.Material.LAB) {
                    if (!(world.getBlockEntity(cell.pos()) instanceof BuildingBlockEntity building)
                            || building.kind() != (cell.material() == StructurePlan.Material.CRATE ? BuildingKind.CRATE : BuildingKind.LAB)) {
                        throw new IllegalStateException("Required building tile missing; restoring footprint.");
                    }
                }
            }
            if (kind == StructureKind.MAIN_BASE) { post.initializeMainBase(); }
            if (data != null) {
                if (kind == StructureKind.MAIN_BASE) { data.registerMainBase(position); } else { data.registerCommandPost(position); }
            }
            for (var cell : plan.cells()) {
                if (cell.material() == StructurePlan.Material.CRATE) {
                    var crate = (BuildingBlockEntity) world.getBlockEntity(cell.pos());
                    int initial = kind == StructureKind.CHECKPOINT ? StructureConfig.CHECKPOINT_SUPPLIES.get() : StructureConfig.MAIN_BASE_SUPPLIES.get();
                    crate.change(crate.state().supplies(Math.min(initial, BuildingConfig.CAPACITY.get())));
                }
            }
            if (kind == StructureKind.CHECKPOINT) {
                post.initializeSite(new SiteProvenance(java.util.UUID.randomUUID(), position.dimension().toString(), plan.commandPost(), radius,
                        world instanceof ServerLevel ? "debug" : "worldgen", 0));
            }
            // Worker-thread worldgen never touches server SavedData. The tile's onLoad registers it later.
            LogUtils.getLogger().debug("Placed {} at {} ({} changed blocks).", kind, position.key(), before.size());
            return new Result(true, "Placed " + kind + " at " + position.key() + "; " + before.size() + " changed blocks.", plan.commandPost());
        } catch (RuntimeException exception) {
            var entries = new ArrayList<>(before.entrySet());
            for (int i = entries.size() - 1; i >= 0; i--) {
                var entry = entries.get(i);
                world.setBlock(entry.getKey(), entry.getValue(), FLAGS);
            }
            LogUtils.getLogger().debug("Skipped {} in chunk {},{}: {}", kind, chunkX, chunkZ, exception.getMessage());
            return new Result(false, exception.getMessage(), null);
        }
    }

    private static BlockState block(StructurePlan.Material material) {
        return switch (material) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case FLOOR -> Blocks.STONE_BRICKS.defaultBlockState();
            case WALL -> Blocks.COBBLESTONE.defaultBlockState();
            case ROOF -> Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
            case LIGHT -> Blocks.TORCH.defaultBlockState();
            case COMMAND_POST -> BuildingRegistry.COMMAND_POST.get().defaultBlockState();
            case LAB -> BuildingRegistry.LAB.get().defaultBlockState();
            case CRATE -> BuildingRegistry.CRATE.get().defaultBlockState();
            case COMMAND_MARKER -> Blocks.CONCRETE.pick(DyeColor.GREEN).defaultBlockState();
            case RESEARCH_MARKER -> Blocks.CONCRETE.pick(DyeColor.CYAN).defaultBlockState();
            case BARRACKS_MARKER -> Blocks.CONCRETE.pick(DyeColor.GRAY).defaultBlockState();
            case SUPPLY_MARKER -> Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState();
        };
    }
}
