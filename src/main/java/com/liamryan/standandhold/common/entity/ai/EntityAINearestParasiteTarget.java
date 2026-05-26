package com.liamryan.standandhold.common.entity.ai;

import com.liamryan.standandhold.common.entity.EntityHumanNpc;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class EntityAINearestParasiteTarget extends EntityAIBase {
    private final EntityHumanNpc humanUnit;
    private final int targetChance;
    private EntityLivingBase nearestTarget;

    public EntityAINearestParasiteTarget(EntityHumanNpc humanUnit, int targetChance) {
        this.humanUnit = humanUnit;
        this.targetChance = Math.max(1, targetChance);
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (humanUnit == null || humanUnit.world == null || humanUnit.getAttackTarget() != null || !humanUnit.isEntityAlive()) {
            return false;
        }

        if (targetChance > 1 && humanUnit.getRNG().nextInt(targetChance) != 0) {
            return false;
        }

        nearestTarget = findNearestParasiteTarget();
        return nearestTarget != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase target = humanUnit.getAttackTarget();
        return isValidTarget(target);
    }

    @Override
    public void startExecuting() {
        humanUnit.setAttackTarget(nearestTarget);
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        nearestTarget = null;
        if (!isValidTarget(humanUnit.getAttackTarget())) {
            humanUnit.setAttackTarget(null);
        }
        super.resetTask();
    }

    private EntityLivingBase findNearestParasiteTarget() {
        double range = Math.max(1.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcFollowRange);
        AxisAlignedBB searchBox = humanUnit.getEntityBoundingBox().grow(range, Math.max(4.0D, range / 2.0D), range);
        List<EntityLivingBase> candidates = humanUnit.world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        if (candidates.isEmpty()) {
            return null;
        }

        Collections.sort(candidates, new Comparator<EntityLivingBase>() {
            @Override
            public int compare(EntityLivingBase first, EntityLivingBase second) {
                double firstDistance = humanUnit.getDistanceSq(first);
                double secondDistance = humanUnit.getDistanceSq(second);
                return Double.compare(firstDistance, secondDistance);
            }
        });

        for (EntityLivingBase candidate : candidates) {
            if (isValidTarget(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isValidTarget(EntityLivingBase target) {
        if (target == null || !target.isEntityAlive() || target == humanUnit) {
            return false;
        }

        if (target instanceof EntityPlayer || target instanceof EntityHumanNpc) {
            return false;
        }

        if (humanUnit.isOnSameTeam(target)) {
            return false;
        }

        ResourceLocation entityId = EntityList.getKey(target);
        return StandAndHoldConfig.isHumanUnitTargetEntity(entityId);
    }
}
