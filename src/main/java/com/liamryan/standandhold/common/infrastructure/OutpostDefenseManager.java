package com.liamryan.standandhold.common.infrastructure;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import com.liamryan.standandhold.common.entity.ModEntities;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public final class OutpostDefenseManager {
    private OutpostDefenseManager() {
    }

    public static void updateOutpostDefense(TileEntityFieldCommandPost commandPost) {
        World world = commandPost.getWorld();
        if (world == null || world.isRemote || !StandAndHoldConfig.infrastructure.enableOutpostDefenderSpawning) {
            return;
        }

        int maxDefenders = Math.max(0, StandAndHoldConfig.infrastructure.outpostMaxDefenders);
        if (maxDefenders <= 0) {
            return;
        }

        long worldTime = world.getTotalWorldTime();
        int spawnInterval = Math.max(1, StandAndHoldConfig.infrastructure.outpostDefenderSpawnInterval);
        long lastSpawnTime = commandPost.getLastDefenderSpawnTime();
        if (lastSpawnTime < 0L) {
            commandPost.setLastDefenderSpawnTime(worldTime);
            return;
        }

        if (worldTime - lastSpawnTime < spawnInterval) {
            return;
        }

        commandPost.setLastDefenderSpawnTime(worldTime);
        BlockPos outpostPos = commandPost.getPos();
        int dimension = world.provider.getDimension();
        int patrolRadius = Math.max(4, StandAndHoldConfig.infrastructure.outpostDefenderPatrolRadius);
        int defenderCount = countAssignedDefenders(world, dimension, outpostPos, patrolRadius);
        if (defenderCount >= maxDefenders) {
            return;
        }

        HumanUnitTier tier = getDefenderTierForCurrentStage(world);
        EntityHumanNpc defender = ModEntities.createHumanUnit(world, tier);
        if (defender == null) {
            return;
        }

        BlockPos spawnPos = findSpawnPosition(world, outpostPos);
        if (spawnPos == null) {
            return;
        }

        defender.setLocationAndAngles(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F,
                0.0F
        );
        defender.assignToOutpost(dimension, outpostPos, patrolRadius);
        defender.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);

        if (!defender.isDead && world.spawnEntity(defender) && StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Outpost at {} spawned defender: {}.", outpostPos, tier.getDisplayName());
        }
    }

    private static int countAssignedDefenders(World world, int dimension, BlockPos outpostPos, int patrolRadius) {
        AxisAlignedBB searchArea = new AxisAlignedBB(outpostPos).grow(patrolRadius, Math.max(4, patrolRadius / 2), patrolRadius);
        List<EntityHumanNpc> defenders = world.getEntitiesWithinAABB(EntityHumanNpc.class, searchArea);
        int count = 0;
        for (EntityHumanNpc defender : defenders) {
            if (defender.isEntityAlive() && defender.isAssignedToOutpost(dimension, outpostPos)) {
                count++;
            }
        }
        return count;
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

    private static BlockPos findSpawnPosition(World world, BlockPos outpostPos) {
        int searchRadius = Math.max(1, StandAndHoldConfig.infrastructure.outpostDefenderSpawnSearchRadius);
        for (int attempt = 0; attempt < 16; attempt++) {
            int offsetX = world.rand.nextInt(searchRadius * 2 + 1) - searchRadius;
            int offsetZ = world.rand.nextInt(searchRadius * 2 + 1) - searchRadius;
            BlockPos candidate = outpostPos.add(offsetX, 1, offsetZ);
            if (canSpawnAt(world, candidate)) {
                return candidate;
            }
        }

        BlockPos fallback = outpostPos.up();
        return canSpawnAt(world, fallback) ? fallback : null;
    }

    private static boolean canSpawnAt(World world, BlockPos pos) {
        return world.isAirBlock(pos)
                && world.isAirBlock(pos.up())
                && !world.isAirBlock(pos.down())
                && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }
}
