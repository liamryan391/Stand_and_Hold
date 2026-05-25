package com.liamryan.standandhold.common.entity;

import com.google.common.base.Predicate;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class EntityHumanNpc extends EntityCreature {
    private static final String TAG_ASSIGNED_OUTPOST = "AssignedOutpost";
    private static final String TAG_ASSIGNED_OUTPOST_DIMENSION = "Dimension";
    private static final String TAG_ASSIGNED_OUTPOST_X = "X";
    private static final String TAG_ASSIGNED_OUTPOST_Y = "Y";
    private static final String TAG_ASSIGNED_OUTPOST_Z = "Z";
    private static final String TAG_ASSIGNED_PATROL_RADIUS = "PatrolRadius";

    private int assignedOutpostDimension = Integer.MIN_VALUE;
    private BlockPos assignedOutpostPos;
    private int assignedPatrolRadius;

    protected EntityHumanNpc(World world) {
        super(world);
        enablePersistence();
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, Math.max(0.0D, StandAndHoldConfig.humanNpcs.humanUnitAttackMoveSpeed), true));
        tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, Math.max(0.0D, StandAndHoldConfig.humanNpcs.humanUnitWanderSpeed)));
        tasks.addTask(5, new EntityAIWander(this, Math.max(0.0D, StandAndHoldConfig.humanNpcs.humanUnitWanderSpeed)));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLivingBase>(
                this,
                EntityLivingBase.class,
                Math.max(1, StandAndHoldConfig.humanNpcs.humanUnitTargetChance),
                true,
                false,
                new ConfiguredParasiteTargetPredicate(this)
        ));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(StandAndHoldConfig.getHumanUnitHealth(getUnitTier()));
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(Math.max(0.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcMovementSpeed));
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.max(1.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcFollowRange));
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(StandAndHoldConfig.getHumanUnitDamage(getUnitTier()));
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> entityClass) {
        return super.canAttackClass(entityClass) && !EntityHumanNpc.class.isAssignableFrom(entityClass);
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && isHumanUnitUnlocked();
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingData) {
        if (!isHumanUnitUnlocked()) {
            setDead();
            return livingData;
        }

        return super.onInitialSpawn(difficulty, livingData);
    }

    public abstract HumanUnitTier getUnitTier();

    public int getRequiredHumanStage() {
        return StandAndHoldConfig.getHumanUnitRequiredStage(getUnitTier());
    }

    public void assignToOutpost(int dimension, BlockPos outpostPos, int patrolRadius) {
        assignedOutpostDimension = dimension;
        assignedOutpostPos = outpostPos == null ? null : outpostPos.toImmutable();
        assignedPatrolRadius = Math.max(1, patrolRadius);
        if (assignedOutpostPos != null) {
            setHomePosAndDistance(assignedOutpostPos, assignedPatrolRadius);
        }
    }

    public boolean isAssignedToOutpost(int dimension, BlockPos outpostPos) {
        return assignedOutpostPos != null
                && assignedOutpostDimension == dimension
                && assignedOutpostPos.equals(outpostPos);
    }

    public boolean hasAssignedOutpost() {
        return assignedOutpostPos != null;
    }

    public int getAssignedOutpostDimension() {
        return assignedOutpostDimension;
    }

    @Nullable
    public BlockPos getAssignedOutpostPos() {
        return assignedOutpostPos;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (assignedOutpostPos != null) {
            NBTTagCompound outpostTag = new NBTTagCompound();
            outpostTag.setInteger(TAG_ASSIGNED_OUTPOST_DIMENSION, assignedOutpostDimension);
            outpostTag.setInteger(TAG_ASSIGNED_OUTPOST_X, assignedOutpostPos.getX());
            outpostTag.setInteger(TAG_ASSIGNED_OUTPOST_Y, assignedOutpostPos.getY());
            outpostTag.setInteger(TAG_ASSIGNED_OUTPOST_Z, assignedOutpostPos.getZ());
            outpostTag.setInteger(TAG_ASSIGNED_PATROL_RADIUS, assignedPatrolRadius);
            compound.setTag(TAG_ASSIGNED_OUTPOST, outpostTag);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey(TAG_ASSIGNED_OUTPOST)) {
            NBTTagCompound outpostTag = compound.getCompoundTag(TAG_ASSIGNED_OUTPOST);
            assignToOutpost(
                    outpostTag.getInteger(TAG_ASSIGNED_OUTPOST_DIMENSION),
                    new BlockPos(
                            outpostTag.getInteger(TAG_ASSIGNED_OUTPOST_X),
                            outpostTag.getInteger(TAG_ASSIGNED_OUTPOST_Y),
                            outpostTag.getInteger(TAG_ASSIGNED_OUTPOST_Z)
                    ),
                    outpostTag.getInteger(TAG_ASSIGNED_PATROL_RADIUS)
            );
        }
    }

    private boolean isHumanUnitUnlocked() {
        if (world == null || world.isRemote || world.getMinecraftServer() == null) {
            return true;
        }

        if (HumanPointManager.getData(world).getStage().getId() < getRequiredHumanStage()) {
            return false;
        }

        if (getUnitTier() == HumanUnitTier.SPECIAL_PARASITE_DIVISION_OPERATIVE
                && StandAndHoldConfig.humanNpcs.enableSpecialParasiteDivisionResearchGate) {
            return ResearchManager.isResearchComplete(world, StandAndHoldConfig.getSpecialParasiteDivisionTrainingResearchId());
        }

        return true;
    }

    private static final class ConfiguredParasiteTargetPredicate implements Predicate<EntityLivingBase> {
        private final EntityHumanNpc unit;

        private ConfiguredParasiteTargetPredicate(EntityHumanNpc unit) {
            this.unit = unit;
        }

        @Override
        public boolean apply(@Nullable EntityLivingBase target) {
            if (target == null || !target.isEntityAlive() || target == unit) {
                return false;
            }

            if (target instanceof EntityPlayer || target instanceof EntityHumanNpc) {
                return false;
            }

            ResourceLocation entityId = EntityList.getKey(target);
            return StandAndHoldConfig.isHumanUnitTargetEntity(entityId);
        }
    }
}
