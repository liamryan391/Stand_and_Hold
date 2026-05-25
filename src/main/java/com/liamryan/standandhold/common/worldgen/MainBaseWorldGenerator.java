package com.liamryan.standandhold.common.worldgen;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.block.ModBlocks;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import com.liamryan.standandhold.common.entity.ModEntities;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MainBaseWorldGenerator implements IWorldGenerator {
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState FLOOR = Blocks.STONEBRICK.getDefaultState();
    private static final IBlockState SUPPORT = Blocks.COBBLESTONE.getDefaultState();
    private static final IBlockState WALL = Blocks.STONEBRICK.getDefaultState();
    private static final IBlockState SPAWN_PAD = Blocks.IRON_BLOCK.getDefaultState();
    private static final IBlockState LIGHT = Blocks.GLOWSTONE.getDefaultState();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        GenerationResult result = tryGenerateAtChunk(world, random, chunkX, chunkZ, false);
        if (result.isGenerated() && StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Generated Main Base at {}. Active: {}. Initial defenders: {}.", result.getOrigin(), result.isActive(), result.getDefendersSpawned());
        }
    }

    public static GenerationResult forceGenerateAtChunk(World world, int chunkX, int chunkZ) {
        return tryGenerateAtChunk(world, world.rand, chunkX, chunkZ, true);
    }

    private static GenerationResult tryGenerateAtChunk(World world, Random random, int chunkX, int chunkZ, boolean force) {
        if (!force && (!StandAndHoldConfig.worldGeneration.enableMainBaseGeneration || !isAllowedDimension(world.provider.getDimension()))) {
            return GenerationResult.skipped("disabled or disallowed dimension");
        }

        int spawnChance = Math.max(1, StandAndHoldConfig.worldGeneration.mainBaseSpawnChance);
        if (!force && spawnChance > 1 && random.nextInt(spawnChance) != 0) {
            return GenerationResult.skipped("spawn chance");
        }

        int width = safeOddDimension(StandAndHoldConfig.worldGeneration.mainBaseWidth);
        int depth = safeOddDimension(StandAndHoldConfig.worldGeneration.mainBaseDepth);
        int wallHeight = clamp(StandAndHoldConfig.worldGeneration.mainBaseWallHeight, 2, 4);
        int originX = chunkX * 16 + (16 - width) / 2;
        int originZ = chunkZ * 16 + (16 - depth) / 2;

        BlockPos origin = findSafeOrigin(world, originX, originZ, width, depth);
        if (origin == null) {
            return GenerationResult.skipped("unsafe terrain");
        }

        return generateMainBase(world, origin, width, depth, wallHeight);
    }

    private static BlockPos findSafeOrigin(World world, int originX, int originZ, int width, int depth) {
        int minSurfaceY = Integer.MAX_VALUE;
        int maxSurfaceY = Integer.MIN_VALUE;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int surfaceY = world.getHeight(originX + x, originZ + z);
                if (surfaceY <= 4 || surfaceY >= world.getActualHeight() - 6) {
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

        int maxTerrainDifference = Math.max(0, StandAndHoldConfig.worldGeneration.mainBaseMaxTerrainHeightDifference);
        if (maxSurfaceY - minSurfaceY > maxTerrainDifference) {
            return null;
        }

        return new BlockPos(originX, maxSurfaceY, originZ);
    }

    private static boolean isSafeGround(IBlockState state) {
        Material material = state.getMaterial();
        return material.isSolid() && !material.isLiquid();
    }

    private static GenerationResult generateMainBase(World world, BlockPos origin, int width, int depth, int wallHeight) {
        int clearHeight = wallHeight + 4;
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

        List<BlockPos> defenderPads = getDefenderPads(origin, width, depth);
        for (BlockPos defenderPad : defenderPads) {
            world.setBlockState(defenderPad, SPAWN_PAD, 2);
        }

        placeLabs(world, origin, width, depth);
        placeLights(world, origin, width, depth, wallHeight);

        boolean active = isMainBaseActive(world);
        BlockPos commandPostPos = origin.add(width / 2, 1, depth / 2);
        placeCommandPost(world, commandPostPos, active);
        int defendersSpawned = active ? spawnInitialDefenders(world, commandPostPos, defenderPads) : 0;
        return GenerationResult.generated(origin, active, defendersSpawned);
    }

    private static void placeLabs(World world, BlockPos origin, int width, int depth) {
        world.setBlockState(origin.add(3, 1, 3), ModBlocks.RESEARCH_LAB.getDefaultState(), 2);
        world.setBlockState(origin.add(width - 4, 1, 3), ModBlocks.RESEARCH_LAB.getDefaultState(), 2);
        world.setBlockState(origin.add(3, 1, depth - 4), ModBlocks.RESEARCH_LAB.getDefaultState(), 2);
        world.setBlockState(origin.add(width - 4, 1, depth - 4), ModBlocks.RESEARCH_LAB.getDefaultState(), 2);
    }

    private static void placeLights(World world, BlockPos origin, int width, int depth, int wallHeight) {
        int y = wallHeight + 1;
        world.setBlockState(origin.add(0, y, 0), LIGHT, 2);
        world.setBlockState(origin.add(width - 1, y, 0), LIGHT, 2);
        world.setBlockState(origin.add(0, y, depth - 1), LIGHT, 2);
        world.setBlockState(origin.add(width - 1, y, depth - 1), LIGHT, 2);
    }

    private static void placeCommandPost(World world, BlockPos pos, boolean active) {
        world.setBlockState(pos, ModBlocks.FIELD_COMMAND_POST.getDefaultState(), 2);
        if (!active) {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityFieldCommandPost) {
            ((TileEntityFieldCommandPost) tileEntity).setUpgradeLevel(FieldCommandPostLevel.MAIN_BASE.getLevel());
        }
    }

    private static int spawnInitialDefenders(World world, BlockPos commandPostPos, List<BlockPos> defenderPads) {
        int maxDefenders = Math.min(defenderPads.size(), Math.max(0, StandAndHoldConfig.worldGeneration.mainBaseInitialDefenders));
        if (maxDefenders <= 0) {
            return 0;
        }

        int patrolRadius = Math.max(8, StandAndHoldConfig.worldGeneration.mainBaseDefenderPatrolRadius);
        HumanUnitTier tier = getDefenderTierForCurrentStage(world);
        int spawned = 0;
        for (BlockPos defenderPad : defenderPads) {
            if (spawned >= maxDefenders) {
                break;
            }

            BlockPos spawnPos = defenderPad.up();
            if (!canSpawnAt(world, spawnPos)) {
                continue;
            }

            EntityHumanNpc defender = ModEntities.createHumanUnit(world, tier);
            if (defender == null) {
                continue;
            }

            defender.setLocationAndAngles(
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    world.rand.nextFloat() * 360.0F,
                    0.0F
            );
            defender.assignToOutpost(world.provider.getDimension(), commandPostPos, patrolRadius);
            defender.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
            if (!defender.isDead && world.spawnEntity(defender)) {
                spawned++;
            }
        }
        return spawned;
    }

    private static HumanUnitTier getDefenderTierForCurrentStage(World world) {
        int currentStage = HumanPointManager.getData(world).getStage().getId();
        HumanUnitTier selectedTier = HumanUnitTier.getLowestTier();
        for (HumanUnitTier tier : HumanUnitTier.values()) {
            if (StandAndHoldConfig.getHumanUnitRequiredStage(tier) <= currentStage) {
                selectedTier = tier;
            }
        }
        return selectedTier;
    }

    private static List<BlockPos> getDefenderPads(BlockPos origin, int width, int depth) {
        List<BlockPos> pads = new ArrayList<BlockPos>();
        pads.add(origin.add(width / 2, 1, 3));
        pads.add(origin.add(width - 4, 1, depth / 2));
        pads.add(origin.add(width / 2, 1, depth - 4));
        pads.add(origin.add(3, 1, depth / 2));
        return pads;
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

    private static boolean canSpawnAt(World world, BlockPos pos) {
        return world.isAirBlock(pos)
                && world.isAirBlock(pos.up())
                && !world.isAirBlock(pos.down())
                && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    private static boolean isMainBaseActive(World world) {
        int activationStage = clamp(StandAndHoldConfig.worldGeneration.mainBaseActivationStage, 0, 6);
        return HumanPointManager.getData(world).getStage().getId() >= activationStage;
    }

    private static boolean isPerimeter(int x, int z, int width, int depth) {
        return x == 0 || z == 0 || x == width - 1 || z == depth - 1;
    }

    private static boolean isGateOpening(int x, int z, int gateCenter) {
        return z == 0 && Math.abs(x - gateCenter) <= 1;
    }

    private static boolean isAllowedDimension(int dimensionId) {
        int[] allowedDimensions = StandAndHoldConfig.worldGeneration.mainBaseAllowedDimensions;
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

    private static int safeOddDimension(int configuredValue) {
        int value = clamp(configuredValue, 11, 15);
        return value % 2 == 0 ? value - 1 : value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class GenerationResult {
        private final boolean generated;
        private final BlockPos origin;
        private final boolean active;
        private final int defendersSpawned;
        private final String reason;

        private GenerationResult(boolean generated, BlockPos origin, boolean active, int defendersSpawned, String reason) {
            this.generated = generated;
            this.origin = origin;
            this.active = active;
            this.defendersSpawned = defendersSpawned;
            this.reason = reason;
        }

        private static GenerationResult generated(BlockPos origin, boolean active, int defendersSpawned) {
            return new GenerationResult(true, origin, active, defendersSpawned, "");
        }

        private static GenerationResult skipped(String reason) {
            return new GenerationResult(false, null, false, 0, reason);
        }

        public boolean isGenerated() {
            return generated;
        }

        public BlockPos getOrigin() {
            return origin;
        }

        public boolean isActive() {
            return active;
        }

        public int getDefendersSpawned() {
            return defendersSpawned;
        }

        public String getReason() {
            return reason;
        }
    }
}
