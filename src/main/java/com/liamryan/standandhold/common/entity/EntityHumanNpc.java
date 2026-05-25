package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.world.World;

public abstract class EntityHumanNpc extends EntityCreature {
    protected EntityHumanNpc(World world) {
        super(world);
        enablePersistence();
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.max(1.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcMaxHealth));
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(Math.max(0.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcMovementSpeed));
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.max(1.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcFollowRange));
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Math.max(0.0D, StandAndHoldConfig.humanNpcs.baseHumanNpcAttackDamage));
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> entityClass) {
        return super.canAttackClass(entityClass) && !EntityHumanNpc.class.isAssignableFrom(entityClass);
    }
}
