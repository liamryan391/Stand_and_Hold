package com.liamryan.standandhold.common.infrastructure;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.EntitySpecialParasiteDivisionOperative;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import com.liamryan.standandhold.common.entity.ModEntities;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.threat.ThreatResponseManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MainBaseManager {
    private MainBaseManager() {
    }

    public static void updateMainBase(TileEntityFieldCommandPost commandPost) {
        World world = commandPost.getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        int dimension = world.provider.getDimension();
        BlockPos commandPostPos = commandPost.getPos();
        HumanWorldData data = HumanPointManager.getData(world);
        if (!data.isMainBaseRegistered(dimension, commandPostPos)) {
            return;
        }

        if (!data.isMainBaseActive(dimension, commandPostPos)) {
            tryActivateMainBase(world, commandPostPos, true);
            return;
        }

        if (commandPost.getUpgradeLevelInfo() != FieldCommandPostLevel.MAIN_BASE) {
            commandPost.setUpgradeLevel(FieldCommandPostLevel.MAIN_BASE.getLevel());
        }

        updateSpecialParasiteDivisionDeployment(commandPost);
    }

    public static ActivationResult tryActivateMainBase(World world, BlockPos commandPostPos, boolean spawnInitialDefenders) {
        if (world == null || commandPostPos == null || world.isRemote) {
            return ActivationResult.failed(ActivationStatus.INVALID_WORLD, 0);
        }

        int dimension = world.provider.getDimension();
        HumanWorldData data = HumanPointManager.getData(world);
        if (!data.isMainBaseRegistered(dimension, commandPostPos)) {
            return ActivationResult.failed(ActivationStatus.NOT_REGISTERED, 0);
        }

        if (data.isMainBaseActive(dimension, commandPostPos)) {
            return ActivationResult.failed(ActivationStatus.ALREADY_ACTIVE, 0);
        }

        if (!canActivateMainBase(world)) {
            return ActivationResult.failed(ActivationStatus.LOCKED, 0);
        }

        if (!world.isBlockLoaded(commandPostPos)) {
            return ActivationResult.failed(ActivationStatus.NOT_LOADED, 0);
        }

        TileEntity tileEntity = world.getTileEntity(commandPostPos);
        if (!(tileEntity instanceof TileEntityFieldCommandPost)) {
            return ActivationResult.failed(ActivationStatus.MISSING_COMMAND_POST, 0);
        }

        TileEntityFieldCommandPost commandPost = (TileEntityFieldCommandPost) tileEntity;
        commandPost.setUpgradeLevel(FieldCommandPostLevel.MAIN_BASE.getLevel());
        data.setMainBaseActive(dimension, commandPostPos, true);
        int defendersSpawned = spawnInitialDefenders ? spawnInitialDefenders(world, commandPostPos) : 0;

        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Activated Main Base at {}. Initial defenders: {}.", commandPostPos, defendersSpawned);
        }

        return ActivationResult.activated(defendersSpawned);
    }

    public static boolean canActivateMainBase(World world) {
        int activationStage = Math.max(5, clamp(StandAndHoldConfig.worldGeneration.mainBaseActivationStage, 0, 6));
        return HumanPointManager.getData(world).getStage().getId() >= activationStage;
    }

    public static boolean isSpecialParasiteDivisionUnlocked(World world) {
        int requiredStage = StandAndHoldConfig.getHumanUnitRequiredStage(HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE);
        if (HumanPointManager.getData(world).getStage().getId() < requiredStage) {
            return false;
        }

        if (!StandAndHoldConfig.humanNpcs.enableSpecialParasiteDivisionResearchGate) {
            return true;
        }

        return ResearchManager.isResearchComplete(world, StandAndHoldConfig.getSpecialParasiteDivisionTrainingResearchId());
    }

    public static int countAssignedSpecialOperatives(World world, BlockPos commandPostPos) {
        int patrolRadius = Math.max(8, StandAndHoldConfig.worldGeneration.mainBaseDefenderPatrolRadius);
        AxisAlignedBB searchArea = new AxisAlignedBB(commandPostPos).grow(patrolRadius, Math.max(6, patrolRadius / 2), patrolRadius);
        List<EntitySpecialParasiteDivisionOperative> operatives = world.getEntitiesWithinAABB(EntitySpecialParasiteDivisionOperative.class, searchArea);
        int count = 0;
        int dimension = world.provider.getDimension();
        for (EntitySpecialParasiteDivisionOperative operative : operatives) {
            if (operative.isEntityAlive() && operative.isAssignedToOutpost(dimension, commandPostPos)) {
                count++;
            }
        }
        return count;
    }

    public static int spawnInitialDefenders(World world, BlockPos commandPostPos) {
        List<BlockPos> defenderPads = getDefenderPads(commandPostPos);
        int maxDefenders = Math.min(defenderPads.size(), Math.max(0, StandAndHoldConfig.worldGeneration.mainBaseInitialDefenders));
        if (maxDefenders <= 0) {
            return 0;
        }

        int spawned = 0;
        for (BlockPos defenderPad : defenderPads) {
            if (spawned >= maxDefenders) {
                break;
            }

            if (spawnUnitAt(world, getDefenderTierForCurrentStage(world), commandPostPos, defenderPad.up())) {
                spawned++;
            }
        }
        return spawned;
    }

    public static List<BlockPos> getDefenderPads(BlockPos commandPostPos) {
        if (commandPostPos == null) {
            return Collections.emptyList();
        }

        int halfWidth = safeOddDimension(StandAndHoldConfig.worldGeneration.mainBaseWidth) / 2;
        int halfDepth = safeOddDimension(StandAndHoldConfig.worldGeneration.mainBaseDepth) / 2;
        List<BlockPos> pads = new ArrayList<BlockPos>();
        pads.add(commandPostPos.add(0, 0, -Math.max(3, halfDepth - 3)));
        pads.add(commandPostPos.add(Math.max(3, halfWidth - 3), 0, 0));
        pads.add(commandPostPos.add(0, 0, Math.max(3, halfDepth - 3)));
        pads.add(commandPostPos.add(-Math.max(3, halfWidth - 3), 0, 0));
        return pads;
    }

    private static void updateSpecialParasiteDivisionDeployment(TileEntityFieldCommandPost commandPost) {
        World world = commandPost.getWorld();
        if (!StandAndHoldConfig.humanNpcs.enableSpecialParasiteDivisionDeployments || !isSpecialParasiteDivisionUnlocked(world)) {
            return;
        }

        int maxOperatives = Math.max(0, StandAndHoldConfig.humanNpcs.specialParasiteDivisionMaxOperativesPerMainBase);
        if (maxOperatives <= 0) {
            return;
        }

        long worldTime = world.getTotalWorldTime();
        int interval = Math.max(1, StandAndHoldConfig.humanNpcs.specialParasiteDivisionDeploymentInterval);
        long lastDeploymentTime = commandPost.getLastSpecialDivisionDeploymentTime();
        if (lastDeploymentTime < 0L) {
            commandPost.setLastSpecialDivisionDeploymentTime(worldTime);
            return;
        }

        if (worldTime - lastDeploymentTime < interval) {
            return;
        }

        commandPost.setLastSpecialDivisionDeploymentTime(worldTime);
        int deploymentChance = Math.max(1, StandAndHoldConfig.humanNpcs.specialParasiteDivisionDeploymentChance);
        if (deploymentChance > 1 && world.rand.nextInt(deploymentChance) != 0) {
            return;
        }

        if (countAssignedSpecialOperatives(world, commandPost.getPos()) >= maxOperatives) {
            return;
        }

        BlockPos spawnPos = findDeploymentPosition(world, commandPost.getPos());
        if (spawnPos == null) {
            return;
        }

        if (spawnUnitAt(world, HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE, commandPost.getPos(), spawnPos)
                && StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Main Base at {} deployed a Special Parasite Division Operative.", commandPost.getPos());
        }
    }

    private static boolean spawnUnitAt(World world, HumanUnitTier tier, BlockPos commandPostPos, BlockPos spawnPos) {
        if (!canSpawnAt(world, spawnPos)) {
            return false;
        }

        EntityHumanNpc unit = ModEntities.createHumanUnit(world, tier);
        if (unit == null) {
            return false;
        }

        unit.setLocationAndAngles(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F,
                0.0F
        );
        unit.assignToOutpost(world.provider.getDimension(), commandPostPos, Math.max(8, StandAndHoldConfig.worldGeneration.mainBaseDefenderPatrolRadius));
        unit.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
        return !unit.isDead && world.spawnEntity(unit);
    }

    private static HumanUnitTier getDefenderTierForCurrentStage(World world) {
        int currentStage = HumanPointManager.getData(world).getStage().getId();
        HumanUnitTier selectedTier = HumanUnitTier.getLowestTier();
        for (HumanUnitTier tier : HumanUnitTier.values()) {
            if (tier == HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE && !isSpecialParasiteDivisionUnlocked(world)) {
                continue;
            }

            if (StandAndHoldConfig.getHumanUnitRequiredStage(tier) <= currentStage) {
                selectedTier = tier;
            }
        }
        return selectedTier;
    }

    private static BlockPos findDeploymentPosition(World world, BlockPos commandPostPos) {
        BlockPos deploymentCenter = ThreatResponseManager.getHighestLoadedThreatTarget(world);
        if (deploymentCenter == null) {
            deploymentCenter = commandPostPos;
        }

        int radius = Math.max(1, StandAndHoldConfig.humanNpcs.specialParasiteDivisionDeploymentRadius);
        for (int attempt = 0; attempt < 24; attempt++) {
            int offsetX = world.rand.nextInt(radius * 2 + 1) - radius;
            int offsetZ = world.rand.nextInt(radius * 2 + 1) - radius;
            int x = deploymentCenter.getX() + offsetX;
            int z = deploymentCenter.getZ() + offsetZ;
            if (!world.isBlockLoaded(new BlockPos(x, deploymentCenter.getY(), z))) {
                continue;
            }

            BlockPos candidate = new BlockPos(x, world.getHeight(x, z), z);
            if (canSpawnAt(world, candidate)) {
                return candidate;
            }
        }

        BlockPos fallback = new BlockPos(deploymentCenter.getX(), world.getHeight(deploymentCenter.getX(), deploymentCenter.getZ()), deploymentCenter.getZ());
        return canSpawnAt(world, fallback) ? fallback : null;
    }

    private static boolean canSpawnAt(World world, BlockPos pos) {
        return world.isAirBlock(pos)
                && world.isAirBlock(pos.up())
                && !world.isAirBlock(pos.down())
                && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    private static int safeOddDimension(int configuredValue) {
        int value = clamp(configuredValue, 11, 15);
        return value % 2 == 0 ? value - 1 : value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public enum ActivationStatus {
        ACTIVATED,
        ALREADY_ACTIVE,
        LOCKED,
        NOT_REGISTERED,
        NOT_LOADED,
        MISSING_COMMAND_POST,
        INVALID_WORLD
    }

    public static final class ActivationResult {
        private final ActivationStatus status;
        private final int defendersSpawned;

        private ActivationResult(ActivationStatus status, int defendersSpawned) {
            this.status = status;
            this.defendersSpawned = defendersSpawned;
        }

        private static ActivationResult activated(int defendersSpawned) {
            return new ActivationResult(ActivationStatus.ACTIVATED, defendersSpawned);
        }

        private static ActivationResult failed(ActivationStatus status, int defendersSpawned) {
            return new ActivationResult(status, defendersSpawned);
        }

        public ActivationStatus getStatus() {
            return status;
        }

        public int getDefendersSpawned() {
            return defendersSpawned;
        }
    }
}
