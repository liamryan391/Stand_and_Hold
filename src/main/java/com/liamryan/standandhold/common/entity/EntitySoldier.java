package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.common.item.ItemPrototypeRangedWeapon;
import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class EntitySoldier extends EntityHumanNpc implements IRangedAttackMob {
    public EntitySoldier(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        if (StandAndHoldConfig.equipment.enableArmyRiflemanRangedWeapon) {
            tasks.addTask(1, new EntityAIAttackRanged(
                    this,
                    Math.max(0.0D, StandAndHoldConfig.humanNpcs.humanUnitAttackMoveSpeed),
                    Math.max(1, StandAndHoldConfig.equipment.armyRiflemanRangedAttackInterval),
                    Math.max(4.0F, StandAndHoldConfig.equipment.armyRiflemanRangedAttackRange)
            ));
        }
    }

    @Override
    public HumanUnitTier getUnitTier() {
        return HumanUnitTier.ARMY_RIFLEMAN;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        if (!isDead && StandAndHoldConfig.equipment.enableArmyRiflemanRangedWeapon) {
            setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.PROTOTYPE_RANGED_WEAPON));
        }
        return result;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        ResourceLocation targetId = EntityList.getKey(target);
        if (StandAndHoldConfig.equipment.enableArmyRiflemanRangedWeapon && StandAndHoldConfig.isHumanUnitTargetEntity(targetId)) {
            ItemPrototypeRangedWeapon.fireFromNpc(world, this, target);
        }
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
    }
}
