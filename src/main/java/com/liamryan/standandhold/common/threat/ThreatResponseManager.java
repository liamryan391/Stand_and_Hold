package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import com.liamryan.standandhold.common.entity.ModEntities;
import com.liamryan.standandhold.common.infrastructure.MainBaseManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ThreatResponseManager {
    private ThreatResponseManager() {
    }

    public static void recordParasiteKill(World world, BlockPos pos) {
        recordThreat(world, pos, ThreatEventType.PARASITE_KILL);
    }

    public static void recordHumanLoss(World world, EntityHumanNpc unit) {
        recordThreat(world, unit.getPosition(), ThreatEventType.HUMAN_LOSS);
    }

    public static void recordOutpostAttack(World world, EntityHumanNpc unit) {
        if (unit == null || !unit.hasAssignedOutpost()) {
            return;
        }
        recordThreat(world, unit.getAssignedOutpostPos(), ThreatEventType.OUTPOST_ATTACK);
    }

    public static void recordOutpostAttackAt(World world, BlockPos outpostPos) {
        recordThreat(world, outpostPos, ThreatEventType.OUTPOST_ATTACK);
    }

    @Nullable
    public static ThreatRecord getThreatRecordAt(World world, BlockPos pos, boolean create) {
        if (world == null || pos == null) {
            return null;
        }

        int regionChunkSize = getRegionChunkSize();
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        int regionX = Math.floorDiv(chunkX, regionChunkSize);
        int regionZ = Math.floorDiv(chunkZ, regionChunkSize);
        HumanWorldData data = HumanPointManager.getData(world);
        ThreatRecord record = create
                ? data.getOrCreateThreatRecord(world.provider.getDimension(), regionX, regionZ)
                : data.getThreatRecord(world.provider.getDimension(), regionX, regionZ);
        if (record != null) {
            applyThreatDecay(world, record);
        }
        return record;
    }

    public static List<ThreatRecord> getThreatRecordsSorted(World world) {
        HumanWorldData data = HumanPointManager.getData(world);
        List<ThreatRecord> records = new ArrayList<ThreatRecord>(data.getThreatRecords());
        for (ThreatRecord record : records) {
            applyThreatDecay(world, record);
        }
        Collections.sort(records, new Comparator<ThreatRecord>() {
            @Override
            public int compare(ThreatRecord first, ThreatRecord second) {
                int scoreCompare = second.getThreatScore() - first.getThreatScore();
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                long timeCompare = second.getLastEventWorldTime() - first.getLastEventWorldTime();
                return timeCompare > 0L ? 1 : (timeCompare < 0L ? -1 : 0);
            }
        });
        return records;
    }

    public static boolean resetThreatRecord(World world, ThreatRecord record) {
        if (world == null || record == null || world.isRemote) {
            return false;
        }

        boolean changed = record.resetThreat(world.getTotalWorldTime());
        if (changed) {
            HumanPointManager.getData(world).markDirty();
        }
        return changed;
    }

    @Nullable
    public static BlockPos getHighestLoadedThreatTarget(World world) {
        for (ThreatRecord record : getThreatRecordsSorted(world)) {
            if (record.getDimension() != world.provider.getDimension()
                    || record.getThreatScore() < StandAndHoldConfig.threatResponse.threatReinforcementThreshold) {
                continue;
            }

            BlockPos center = getLoadedThreatCenter(world, record);
            if (center != null) {
                return center;
            }
        }
        return null;
    }

    public static ReinforcementResult triggerReinforcement(World world, ThreatRecord record, boolean ignoreThreshold) {
        if (world == null || record == null || world.isRemote) {
            return ReinforcementResult.failed(ReinforcementStatus.INVALID_TARGET, 0);
        }

        if (!StandAndHoldConfig.threatResponse.enableThreatReinforcements) {
            return ReinforcementResult.failed(ReinforcementStatus.DISABLED, 0);
        }

        if (record.getDimension() != world.provider.getDimension()) {
            return ReinforcementResult.failed(ReinforcementStatus.INVALID_TARGET, 0);
        }

        applyThreatDecay(world, record);
        if (!ignoreThreshold && record.getThreatScore() < StandAndHoldConfig.threatResponse.threatReinforcementThreshold) {
            return ReinforcementResult.failed(ReinforcementStatus.BELOW_THRESHOLD, 0);
        }

        int maxReinforcements = Math.max(0, StandAndHoldConfig.threatResponse.maxReinforcementsPerThreatRegion);
        if (maxReinforcements > 0 && record.getReinforcementsSent() >= maxReinforcements) {
            return ReinforcementResult.failed(ReinforcementStatus.REGION_LIMIT_REACHED, 0);
        }

        long worldTime = world.getTotalWorldTime();
        int cooldown = Math.max(0, StandAndHoldConfig.threatResponse.reinforcementCooldownTicks);
        if (!ignoreThreshold && record.getLastReinforcementWorldTime() >= 0L && worldTime - record.getLastReinforcementWorldTime() < cooldown) {
            return ReinforcementResult.failed(ReinforcementStatus.COOLDOWN, 0);
        }

        BlockPos targetCenter = getLoadedThreatCenter(world, record);
        if (targetCenter == null) {
            return ReinforcementResult.failed(ReinforcementStatus.TARGET_NOT_LOADED, 0);
        }

        BlockPos sourceMainBase = findNearestActiveMainBase(world, targetCenter);
        if (sourceMainBase == null) {
            return ReinforcementResult.failed(ReinforcementStatus.NO_ACTIVE_MAIN_BASE, 0);
        }

        int requestedUnits = Math.max(1, StandAndHoldConfig.threatResponse.reinforcementUnitsPerTrigger);
        if (maxReinforcements > 0) {
            requestedUnits = Math.min(requestedUnits, Math.max(0, maxReinforcements - record.getReinforcementsSent()));
        }

        int spawned = 0;
        for (int i = 0; i < requestedUnits; i++) {
            BlockPos spawnPos = findSpawnPositionNear(world, targetCenter);
            if (spawnPos == null) {
                continue;
            }

            if (spawnUnitAt(world, selectReinforcementTier(world, record), sourceMainBase, targetCenter, spawnPos)) {
                spawned++;
            }
        }

        if (spawned <= 0) {
            return ReinforcementResult.failed(ReinforcementStatus.NO_SAFE_SPAWN, 0);
        }

        record.recordReinforcements(spawned, worldTime);
        HumanPointManager.getData(world).markDirty();
        if (StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Threat response deployed {} reinforcements to region {}.", spawned, record.getKey());
        }
        return ReinforcementResult.success(spawned);
    }

    private static void recordThreat(World world, BlockPos pos, ThreatEventType eventType) {
        if (world == null || pos == null || world.isRemote || !StandAndHoldConfig.threatResponse.enableThreatTracking) {
            return;
        }

        ThreatRecord record = getThreatRecordAt(world, pos, true);
        if (record == null) {
            return;
        }

        long worldTime = world.getTotalWorldTime();
        applyThreatDecay(world, record);
        int maxThreatScore = Math.max(1, StandAndHoldConfig.threatResponse.maxThreatScore);
        switch (eventType) {
            case PARASITE_KILL:
                record.recordParasiteKill(StandAndHoldConfig.threatResponse.parasiteKillThreatIncrease, worldTime, maxThreatScore);
                break;
            case HUMAN_LOSS:
                record.recordHumanLoss(StandAndHoldConfig.threatResponse.humanLossThreatIncrease, worldTime, maxThreatScore);
                break;
            case OUTPOST_ATTACK:
                int cooldown = Math.max(0, StandAndHoldConfig.threatResponse.outpostAttackThreatCooldownTicks);
                if (record.getLastOutpostAttackWorldTime() >= 0L && worldTime - record.getLastOutpostAttackWorldTime() < cooldown) {
                    return;
                }
                record.recordOutpostAttack(StandAndHoldConfig.threatResponse.outpostAttackThreatIncrease, worldTime, maxThreatScore);
                break;
            default:
                return;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        data.pruneThreatRecords(Math.max(1, StandAndHoldConfig.threatResponse.maxThreatRecords));
        data.markDirty();
        triggerReinforcement(world, record, false);
    }

    private static boolean applyThreatDecay(World world, ThreatRecord record) {
        if (world == null || world.isRemote || record == null || !StandAndHoldConfig.threatResponse.enableThreatDecay) {
            return false;
        }

        int decayAmount = Math.max(0, StandAndHoldConfig.threatResponse.threatDecayAmount);
        int decayInterval = Math.max(1, StandAndHoldConfig.threatResponse.threatDecayIntervalTicks);
        if (decayAmount <= 0) {
            return false;
        }

        long worldTime = world.getTotalWorldTime();
        long lastDecayTime = record.getLastDecayWorldTime();
        if (lastDecayTime < 0L) {
            record.decayThreat(0, worldTime);
            HumanPointManager.getData(world).markDirty();
            return false;
        }

        long elapsed = worldTime - lastDecayTime;
        if (elapsed < decayInterval) {
            return false;
        }

        int steps = (int) Math.min(Integer.MAX_VALUE, elapsed / decayInterval);
        int totalDecay = steps > 0 && decayAmount > Integer.MAX_VALUE / steps ? Integer.MAX_VALUE : decayAmount * steps;
        boolean changed = record.decayThreat(totalDecay, worldTime);
        if (changed) {
            HumanPointManager.getData(world).markDirty();
        }
        return changed;
    }

    @Nullable
    private static BlockPos getLoadedThreatCenter(World world, ThreatRecord record) {
        BlockPos center = record.getApproximateCenterBlock(getRegionChunkSize());
        if (!world.isBlockLoaded(center)) {
            return null;
        }

        int surfaceY = world.getHeight(center.getX(), center.getZ());
        BlockPos surface = new BlockPos(center.getX(), surfaceY, center.getZ());
        return world.isBlockLoaded(surface) ? surface : null;
    }

    @Nullable
    private static BlockPos findNearestActiveMainBase(World world, BlockPos targetPos) {
        HumanWorldData data = HumanPointManager.getData(world);
        data.cleanupLoadedMainBasePositions(world);
        int dimension = world.provider.getDimension();
        BlockPos nearest = null;
        long nearestDistance = Long.MAX_VALUE;

        for (String positionKey : data.getActiveMainBasePositions()) {
            PositionRecord record = PositionRecord.parse(positionKey);
            if (record == null || record.dimension != dimension || !world.isBlockLoaded(record.pos)) {
                continue;
            }

            long distance = distanceSq(targetPos, record.pos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = record.pos;
            }
        }
        return nearest;
    }

    @Nullable
    private static BlockPos findSpawnPositionNear(World world, BlockPos center) {
        int radius = Math.max(1, StandAndHoldConfig.threatResponse.reinforcementSpawnRadius);
        for (int attempt = 0; attempt < 24; attempt++) {
            int offsetX = world.rand.nextInt(radius * 2 + 1) - radius;
            int offsetZ = world.rand.nextInt(radius * 2 + 1) - radius;
            int x = center.getX() + offsetX;
            int z = center.getZ() + offsetZ;
            if (!world.isBlockLoaded(new BlockPos(x, center.getY(), z))) {
                continue;
            }

            BlockPos candidate = new BlockPos(x, world.getHeight(x, z), z);
            if (canSpawnAt(world, candidate)) {
                return candidate;
            }
        }
        return canSpawnAt(world, center) ? center : null;
    }

    private static boolean spawnUnitAt(World world, HumanUnitTier tier, BlockPos assignedBase, BlockPos patrolTarget, BlockPos spawnPos) {
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
        unit.assignToOutpost(world.provider.getDimension(), assignedBase, Math.max(8, StandAndHoldConfig.threatResponse.reinforcementPatrolRadius));
        unit.assignThreatPatrol(world.provider.getDimension(), patrolTarget, Math.max(8, StandAndHoldConfig.threatResponse.reinforcementPatrolRadius));
        unit.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
        return !unit.isDead && world.spawnEntity(unit);
    }

    private static HumanUnitTier selectReinforcementTier(World world, ThreatRecord record) {
        int currentStage = HumanPointManager.getData(world).getStage().getId();
        HumanUnitTier selectedTier = HumanUnitTier.getLowestTier();
        for (HumanUnitTier tier : HumanUnitTier.values()) {
            if (tier == HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE
                    && (record.getThreatLevel() != ThreatLevel.CRITICAL || !MainBaseManager.isSpecialParasiteDivisionUnlocked(world))) {
                continue;
            }

            if (StandAndHoldConfig.getHumanUnitRequiredStage(tier) <= currentStage) {
                selectedTier = tier;
            }
        }
        return selectedTier;
    }

    private static boolean canSpawnAt(World world, BlockPos pos) {
        return world.isBlockLoaded(pos)
                && world.isAirBlock(pos)
                && world.isAirBlock(pos.up())
                && !world.isAirBlock(pos.down())
                && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    private static long distanceSq(BlockPos first, BlockPos second) {
        long dx = first.getX() - second.getX();
        long dy = first.getY() - second.getY();
        long dz = first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static int getRegionChunkSize() {
        return Math.max(1, StandAndHoldConfig.threatResponse.threatRegionChunkSize);
    }

    private enum ThreatEventType {
        PARASITE_KILL,
        HUMAN_LOSS,
        OUTPOST_ATTACK
    }

    public enum ReinforcementStatus {
        DEPLOYED,
        DISABLED,
        BELOW_THRESHOLD,
        COOLDOWN,
        REGION_LIMIT_REACHED,
        TARGET_NOT_LOADED,
        NO_ACTIVE_MAIN_BASE,
        NO_SAFE_SPAWN,
        INVALID_TARGET
    }

    public static final class ReinforcementResult {
        private final ReinforcementStatus status;
        private final int unitsSpawned;

        private ReinforcementResult(ReinforcementStatus status, int unitsSpawned) {
            this.status = status;
            this.unitsSpawned = unitsSpawned;
        }

        private static ReinforcementResult success(int unitsSpawned) {
            return new ReinforcementResult(ReinforcementStatus.DEPLOYED, unitsSpawned);
        }

        private static ReinforcementResult failed(ReinforcementStatus status, int unitsSpawned) {
            return new ReinforcementResult(status, unitsSpawned);
        }

        public ReinforcementStatus getStatus() {
            return status;
        }

        public int getUnitsSpawned() {
            return unitsSpawned;
        }
    }

    private static final class PositionRecord {
        private final int dimension;
        private final BlockPos pos;

        private PositionRecord(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        @Nullable
        private static PositionRecord parse(String positionKey) {
            if (positionKey == null || positionKey.trim().isEmpty()) {
                return null;
            }

            String[] parts = positionKey.split(":");
            if (parts.length != 4) {
                return null;
            }

            try {
                int dimension = Integer.parseInt(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                return new PositionRecord(dimension, new BlockPos(x, y, z));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
