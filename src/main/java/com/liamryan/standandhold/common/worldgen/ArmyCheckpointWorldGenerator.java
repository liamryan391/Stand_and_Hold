package com.liamryan.standandhold.common.worldgen;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.block.ModBlocks;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public final class ArmyCheckpointWorldGenerator implements IWorldGenerator {
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState FLOOR = Blocks.STONEBRICK.getDefaultState();
    private static final IBlockState SUPPORT = Blocks.COBBLESTONE.getDefaultState();
    private static final IBlockState WALL = Blocks.COBBLESTONE_WALL.getDefaultState();
    private static final IBlockState TORCH = Blocks.TORCH.getDefaultState();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        BlockPos origin = tryGenerateAtChunk(world, random, chunkX, chunkZ, false);
        if (origin != null && StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Generated Small Army Checkpoint at {}.", origin);
        }
    }

    public static BlockPos forceGenerateAtChunk(World world, int chunkX, int chunkZ) {
        return tryGenerateAtChunk(world, world.rand, chunkX, chunkZ, true);
    }

    private static BlockPos tryGenerateAtChunk(World world, Random random, int chunkX, int chunkZ, boolean force) {
        if (!force && (!StandAndHoldConfig.worldGeneration.enableArmyCheckpointGeneration || !isAllowedDimension(world.provider.getDimension()))) {
            return null;
        }

        int spawnChance = Math.max(1, StandAndHoldConfig.worldGeneration.armyCheckpointSpawnChance);
        if (!force && spawnChance > 1 && random.nextInt(spawnChance) != 0) {
            return null;
        }

        int width = clamp(StandAndHoldConfig.worldGeneration.armyCheckpointWidth, 5, 14);
        int depth = clamp(StandAndHoldConfig.worldGeneration.armyCheckpointDepth, 5, 14);
        int wallHeight = clamp(StandAndHoldConfig.worldGeneration.armyCheckpointWallHeight, 1, 3);
        int originX = chunkX * 16 + (16 - width) / 2;
        int originZ = chunkZ * 16 + (16 - depth) / 2;

        BlockPos origin = findSafeOrigin(world, originX, originZ, width, depth);
        if (origin == null) {
            return null;
        }

        generateCheckpoint(world, origin, width, depth, wallHeight);
        return origin;
    }

    private static BlockPos findSafeOrigin(World world, int originX, int originZ, int width, int depth) {
        int minSurfaceY = Integer.MAX_VALUE;
        int maxSurfaceY = Integer.MIN_VALUE;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int surfaceY = world.getHeight(originX + x, originZ + z);
                if (surfaceY <= 4 || surfaceY >= world.getActualHeight() - 4) {
                    return null;
                }

                BlockPos groundPos = new BlockPos(originX + x, surfaceY - 1, originZ + z);
                if (!isSafeGround(world.getBlockState(groundPos))) {
                    return null;
                }

                minSurfaceY = Math.min(minSurfaceY, surfaceY);
                maxSurfaceY = Math.max(maxSurfaceY, surfaceY);
            }
        }

        int maxTerrainDifference = Math.max(0, StandAndHoldConfig.worldGeneration.armyCheckpointMaxTerrainHeightDifference);
        if (maxSurfaceY - minSurfaceY > maxTerrainDifference) {
            return null;
        }

        return new BlockPos(originX, maxSurfaceY, originZ);
    }

    private static boolean isSafeGround(IBlockState state) {
        Material material = state.getMaterial();
        return material.isSolid() && !material.isLiquid();
    }

    private static void generateCheckpoint(World world, BlockPos origin, int width, int depth, int wallHeight) {
        int clearHeight = wallHeight + 3;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos floorPos = origin.add(x, 0, z);
                fillSupportToFloor(world, floorPos);
                clearColumn(world, floorPos.up(), clearHeight);
                world.setBlockState(floorPos, FLOOR, 2);
            }
        }

        int gateCenter = width / 2;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                if (!isPerimeter(x, z, width, depth) || isGateOpening(x, z, gateCenter)) {
                    continue;
                }

                for (int y = 1; y <= wallHeight; y++) {
                    world.setBlockState(origin.add(x, y, z), WALL, 2);
                }
            }
        }

        placeTorches(world, origin, width, depth, wallHeight);
        world.setBlockState(origin.add(width / 2, 1, depth / 2), ModBlocks.FIELD_COMMAND_POST.getDefaultState(), 2);
    }

    private static void fillSupportToFloor(World world, BlockPos floorPos) {
        int surfaceY = world.getHeight(floorPos.getX(), floorPos.getZ());
        for (int y = surfaceY; y < floorPos.getY(); y++) {
            world.setBlockState(new BlockPos(floorPos.getX(), y, floorPos.getZ()), SUPPORT, 2);
        }
    }

    private static void clearColumn(World world, BlockPos startPos, int height) {
        for (int y = 0; y < height; y++) {
            BlockPos clearPos = startPos.up(y);
            if (!world.isAirBlock(clearPos)) {
                world.setBlockState(clearPos, AIR, 2);
            }
        }
    }

    private static boolean isPerimeter(int x, int z, int width, int depth) {
        return x == 0 || z == 0 || x == width - 1 || z == depth - 1;
    }

    private static boolean isGateOpening(int x, int z, int gateCenter) {
        return z == 0 && Math.abs(x - gateCenter) <= 1;
    }

    private static void placeTorches(World world, BlockPos origin, int width, int depth, int wallHeight) {
        int y = wallHeight + 1;
        world.setBlockState(origin.add(0, y, 0), TORCH, 2);
        world.setBlockState(origin.add(width - 1, y, 0), TORCH, 2);
        world.setBlockState(origin.add(0, y, depth - 1), TORCH, 2);
        world.setBlockState(origin.add(width - 1, y, depth - 1), TORCH, 2);
    }

    private static boolean isAllowedDimension(int dimensionId) {
        int[] allowedDimensions = StandAndHoldConfig.worldGeneration.armyCheckpointAllowedDimensions;
        if (allowedDimensions == null || allowedDimensions.length == 0) {
            return dimensionId == 0;
        }

        for (int allowedDimension : allowedDimensions) {
            if (allowedDimension == dimensionId) {
                return true;
            }
        }
        return false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
