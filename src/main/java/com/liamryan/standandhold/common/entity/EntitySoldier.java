package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class EntitySoldier extends EntityHumanNpc {
    public EntitySoldier(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        tasks.addTask(2, new EntityAIAttackMelee(this, StandAndHoldConfig.humanNpcs.soldierAttackMoveSpeed, true));
        tasks.addTask(5, new EntityAIWander(this, StandAndHoldConfig.humanNpcs.soldierWanderSpeed));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLivingBase>(
                this,
                EntityLivingBase.class,
                Math.max(1, StandAndHoldConfig.humanNpcs.soldierTargetChance),
                true,
                false,
                new ConfiguredParasiteTargetPredicate(this)
        ));
    }

    private static final class ConfiguredParasiteTargetPredicate implements Predicate<EntityLivingBase> {
        private final EntitySoldier soldier;

        private ConfiguredParasiteTargetPredicate(EntitySoldier soldier) {
            this.soldier = soldier;
        }

        @Override
        public boolean apply(@Nullable EntityLivingBase target) {
            if (target == null || !target.isEntityAlive() || target == soldier) {
                return false;
            }

            if (target instanceof EntityPlayer || target instanceof EntityHumanNpc) {
                return false;
            }

            ResourceLocation entityId = EntityList.getKey(target);
            return StandAndHoldConfig.isSoldierTargetEntity(entityId);
        }
    }
}
