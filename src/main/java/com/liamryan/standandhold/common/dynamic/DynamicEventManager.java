package com.liamryan.standandhold.common.dynamic;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.common.entity.HumanUnitTier;
import com.liamryan.standandhold.common.entity.ModEntities;
import com.liamryan.standandhold.common.infrastructure.MainBaseManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.threat.ThreatResponseManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class DynamicEventManager {
    private DynamicEventManager() {
    }

    public static void updateCommandPostEvents(TileEntityFieldCommandPost commandPost) {
        if (commandPost == null
                || !StandAndHoldConfig.dynamicEvents.enableDynamicEvents
                || !StandAndHoldConfig.dynamicEvents.enableNaturalDynamicEvents) {
            return;
        }

        World world = commandPost.getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        long worldTime = world.getTotalWorldTime();
        long lastEventTime = commandPost.getLastDynamicEventTime();
        if (lastEventTime < 0L) {
            commandPost.setLastDynamicEventTime(worldTime);
            return;
        }

        int interval = Math.max(1, StandAndHoldConfig.dynamicEvents.dynamicEventIntervalTicks);
        if (worldTime - lastEventTime < interval) {
            return;
        }

        commandPost.setLastDynamicEventTime(worldTime);
        int chance = Math.max(1, StandAndHoldConfig.dynamicEvents.dynamicEventChance);
        if (world.rand.nextInt(chance) != 0) {
            return;
        }

        DynamicEventResult result = triggerWeightedNaturalEvent(world, commandPost.getPos());
        if (result.getStatus() == DynamicEventStatus.STARTED && StandAndHoldConfig.debugLogging) {
            StandAndHold.LOGGER.info("Dynamic event started at {}: {} spawned {} entities.",
                    commandPost.getPos(),
                    result.getType().getDisplayName(),
                    result.getSpawnedCount());
        }
    }

    public static DynamicEventResult triggerOutpostAttack(World world, BlockPos outpostPos, boolean forced) {
        if (!canStart(world, outpostPos, forced)) {
            return DynamicEventResult.failed(DynamicEventType.OUTPOST_ATTACK, DynamicEventStatus.DISABLED, outpostPos, getStageId(world));
        }

        List<ResourceLocation> attackerEntityIds = getConfiguredAttackerEntityIds();
        if (attackerEntityIds.isEmpty()) {
            return DynamicEventResult.failed(DynamicEventType.OUTPOST_ATTACK, DynamicEventStatus.NO_CONFIGURED_ATTACKERS, outpostPos, getStageId(world));
        }

        int stageId = getStageId(world);
        int requestedAttackers = getStageScaledCount(
                StandAndHoldConfig.dynamicEvents.outpostAttackBaseCount,
                StandAndHoldConfig.dynamicEvents.outpostAttackersPerStage,
                StandAndHoldConfig.dynamicEvents.outpostAttackMaxCount,
                stageId
        );
        if (requestedAttackers <= 0) {
            return DynamicEventResult.failed(DynamicEventType.OUTPOST_ATTACK, DynamicEventStatus.NO_UNITS, outpostPos, stageId);
        }

        int spawned = 0;
        for (int i = 0; i < requestedAttackers; i++) {
            ResourceLocation attackerEntityId = attackerEntityIds.get(world.rand.nextInt(attackerEntityIds.size()));
            BlockPos spawnPos = findSpawnPosition(world, outpostPos);
            if (spawnPos != null && spawnAttackEntity(world, attackerEntityId, spawnPos, outpostPos)) {
                spawned++;
            }
        }

        if (spawned <= 0) {
            return DynamicEventResult.failed(DynamicEventType.OUTPOST_ATTACK, DynamicEventStatus.NO_SAFE_SPAWN, outpostPos, stageId);
        }

        ThreatResponseManager.recordOutpostAttackAt(world, outpostPos);
        return DynamicEventResult.started(DynamicEventType.OUTPOST_ATTACK, outpostPos, stageId, spawned);
    }

    public static DynamicEventResult triggerHumanReinforcement(World world, BlockPos outpostPos, boolean forced) {
        if (!canStart(world, outpostPos, forced)) {
            return DynamicEventResult.failed(DynamicEventType.HUMAN_REINFORCEMENT, DynamicEventStatus.DISABLED, outpostPos, getStageId(world));
        }

        int stageId = getStageId(world);
        int requestedUnits = getStageScaledCount(
                StandAndHoldConfig.dynamicEvents.humanReinforcementBaseCount,
                StandAndHoldConfig.dynamicEvents.humanReinforcementsPerStage,
                StandAndHoldConfig.dynamicEvents.humanReinforcementMaxCount,
                stageId
        );
        if (requestedUnits <= 0) {
            return DynamicEventResult.failed(DynamicEventType.HUMAN_REINFORCEMENT, DynamicEventStatus.NO_UNITS, outpostPos, stageId);
        }

        int spawned = 0;
        HumanUnitTier tier = selectReinforcementTier(world);
        int patrolRadius = Math.max(4, StandAndHoldConfig.dynamicEvents.humanReinforcementPatrolRadius);
        for (int i = 0; i < requestedUnits; i++) {
            BlockPos spawnPos = findSpawnPosition(world, outpostPos);
            if (spawnPos != null && spawnHumanUnit(world, tier, outpostPos, patrolRadius, spawnPos)) {
                spawned++;
            }
        }

        if (spawned <= 0) {
            return DynamicEventResult.failed(DynamicEventType.HUMAN_REINFORCEMENT, DynamicEventStatus.NO_SAFE_SPAWN, outpostPos, stageId);
        }

        return DynamicEventResult.started(DynamicEventType.HUMAN_REINFORCEMENT, outpostPos, stageId, spawned);
    }

    private static DynamicEventResult triggerWeightedNaturalEvent(World world, BlockPos outpostPos) {
        int attackWeight = Math.max(0, StandAndHoldConfig.dynamicEvents.outpostAttackWeight);
        int reinforcementWeight = Math.max(0, StandAndHoldConfig.dynamicEvents.humanReinforcementWeight);
        int totalWeight = attackWeight + reinforcementWeight;
        if (totalWeight <= 0) {
            return DynamicEventResult.failed(DynamicEventType.NONE, DynamicEventStatus.DISABLED, outpostPos, getStageId(world));
        }

        int roll = world.rand.nextInt(totalWeight);
        if (roll < attackWeight) {
            return triggerOutpostAttack(world, outpostPos, false);
        }
        return triggerHumanReinforcement(world, outpostPos, false);
    }

    private static boolean canStart(World world, BlockPos pos, boolean forced) {
        return world != null
                && !world.isRemote
                && pos != null
                && world.isBlockLoaded(pos)
                && StandAndHoldConfig.dynamicEvents.enableDynamicEvents;
    }

    private static int getStageScaledCount(int baseCount, int perStage, int maxCount, int stageId) {
        int requested = Math.max(0, baseCount) + Math.max(0, perStage) * Math.max(0, stageId);
        int safeMax = Math.max(0, maxCount);
        return safeMax <= 0 ? requested : Math.min(requested, safeMax);
    }

    private static int getStageId(World world) {
        if (world == null || world.isRemote) {
            return 0;
        }
        return HumanPointManager.getData(world).getStage().getId();
    }

    private static List<ResourceLocation> getConfiguredAttackerEntityIds() {
        List<ResourceLocation> entityIds = new ArrayList<ResourceLocation>();
        String[] configuredEntityIds = StandAndHoldConfig.dynamicEvents.outpostAttackEntityIds;
        if (configuredEntityIds == null) {
            return entityIds;
        }

        for (String configuredEntityId : configuredEntityIds) {
            ResourceLocation entityId = parseEntityId(configuredEntityId);
            if (entityId != null) {
                entityIds.add(entityId);
            }
        }
        return entityIds;
    }

    @Nullable
    private static ResourceLocation parseEntityId(String rawEntityId) {
        if (rawEntityId == null || rawEntityId.trim().isEmpty()) {
            return null;
        }

        try {
            return new ResourceLocation(rawEntityId.trim());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean spawnAttackEntity(World world, ResourceLocation entityId, BlockPos spawnPos, BlockPos outpostPos) {
        Entity entity = EntityList.createEntityByIDFromName(entityId, world);
        if (entity == null) {
            return false;
        }

        entity.setLocationAndAngles(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F,
                0.0F
        );
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
            EntityLivingBase target = findNearestHumanTarget(world, outpostPos);
            if (target != null) {
                living.setAttackTarget(target);
            }
        }
        return world.spawnEntity(entity);
    }

    @Nullable
    private static EntityLivingBase findNearestHumanTarget(World world, BlockPos outpostPos) {
        int radius = Math.max(8, StandAndHoldConfig.dynamicEvents.dynamicEventSpawnRadius + 8);
        AxisAlignedBB searchArea = new AxisAlignedBB(outpostPos).grow(radius);
        List<EntityHumanNpc> candidates = world.getEntitiesWithinAABB(EntityHumanNpc.class, searchArea);
        EntityHumanNpc nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityHumanNpc candidate : candidates) {
            if (candidate == null || !candidate.isEntityAlive()) {
                continue;
            }

            double distance = candidate.getDistanceSq(
                    outpostPos.getX() + 0.5D,
                    outpostPos.getY() + 0.5D,
                    outpostPos.getZ() + 0.5D
            );
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static boolean spawnHumanUnit(World world, HumanUnitTier tier, BlockPos outpostPos, int patrolRadius, BlockPos spawnPos) {
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
        unit.assignToOutpost(world.provider.getDimension(), outpostPos, patrolRadius);
        unit.onInitialSpawn(world.getDifficultyForLocation(spawnPos), null);
        return !unit.isDead && world.spawnEntity(unit);
    }

    private static HumanUnitTier selectReinforcementTier(World world) {
        int currentStage = getStageId(world);
        HumanUnitTier selectedTier = HumanUnitTier.getLowestTier();
        for (HumanUnitTier tier : HumanUnitTier.values()) {
            if (tier == HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE && !MainBaseManager.isSpecialParasiteDivisionUnlocked(world)) {
                continue;
            }

            if (StandAndHoldConfig.getHumanUnitRequiredStage(tier) <= currentStage) {
                selectedTier = tier;
            }
        }
        return selectedTier;
    }

    @Nullable
    private static BlockPos findSpawnPosition(World world, BlockPos anchorPos) {
        int radius = Math.max(1, StandAndHoldConfig.dynamicEvents.dynamicEventSpawnRadius);
        for (int attempt = 0; attempt < 24; attempt++) {
            int offsetX = world.rand.nextInt(radius * 2 + 1) - radius;
            int offsetZ = world.rand.nextInt(radius * 2 + 1) - radius;
            int x = anchorPos.getX() + offsetX;
            int z = anchorPos.getZ() + offsetZ;
            BlockPos candidate = new BlockPos(x, world.getHeight(x, z), z);
            if (canSpawnAt(world, candidate)) {
                return candidate;
            }
        }

        BlockPos fallback = new BlockPos(anchorPos.getX(), world.getHeight(anchorPos.getX(), anchorPos.getZ()), anchorPos.getZ());
        return canSpawnAt(world, fallback) ? fallback : null;
    }

    private static boolean canSpawnAt(World world, BlockPos pos) {
        return world.isBlockLoaded(pos)
                && world.isAirBlock(pos)
                && world.isAirBlock(pos.up())
                && !world.isAirBlock(pos.down())
                && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    public enum DynamicEventType {
        NONE("Dynamic Event"),
        OUTPOST_ATTACK("Outpost Attack"),
        HUMAN_REINFORCEMENT("Human Reinforcement");

        private final String displayName;

        DynamicEventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DynamicEventStatus {
        STARTED,
        DISABLED,
        NO_CONFIGURED_ATTACKERS,
        NO_SAFE_SPAWN,
        NO_UNITS
    }

    public static final class DynamicEventResult {
        private final DynamicEventType type;
        private final DynamicEventStatus status;
        private final BlockPos pos;
        private final int stageId;
        private final int spawnedCount;

        private DynamicEventResult(DynamicEventType type, DynamicEventStatus status, BlockPos pos, int stageId, int spawnedCount) {
            this.type = type;
            this.status = status;
            this.pos = pos == null ? BlockPos.ORIGIN : pos.toImmutable();
            this.stageId = Math.max(0, stageId);
            this.spawnedCount = Math.max(0, spawnedCount);
        }

        private static DynamicEventResult started(DynamicEventType type, BlockPos pos, int stageId, int spawnedCount) {
            return new DynamicEventResult(type, DynamicEventStatus.STARTED, pos, stageId, spawnedCount);
        }

        private static DynamicEventResult failed(DynamicEventType type, DynamicEventStatus status, BlockPos pos, int stageId) {
            return new DynamicEventResult(type, status, pos, stageId, 0);
        }

        public DynamicEventType getType() {
            return type;
        }

        public DynamicEventStatus getStatus() {
            return status;
        }

        public BlockPos getPos() {
            return pos;
        }

        public int getStageId() {
            return stageId;
        }

        public int getSpawnedCount() {
            return spawnedCount;
        }
    }
}
