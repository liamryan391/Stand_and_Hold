package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class EntityAntiParasiteProjectile extends EntityThrowable {
    private static final String TAG_PROJECTILE_DAMAGE = "ProjectileDamage";

    private float projectileDamage = Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponDamage);

    public EntityAntiParasiteProjectile(World world) {
        super(world);
    }

    public EntityAntiParasiteProjectile(World world, EntityLivingBase thrower, float projectileDamage) {
        super(world, thrower);
        this.projectileDamage = Math.max(0.0F, projectileDamage);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            Entity hitEntity = result.entityHit;
            if (hitEntity instanceof EntityLivingBase && canDamageEntity(hitEntity)) {
                EntityLivingBase thrower = getThrower();
                hitEntity.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower == null ? this : thrower), projectileDamage);
            }
            setDead();
        }
    }

    @Override
    protected float getGravityVelocity() {
        return 0.02F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat(TAG_PROJECTILE_DAMAGE, projectileDamage);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        projectileDamage = compound.hasKey(TAG_PROJECTILE_DAMAGE) ? Math.max(0.0F, compound.getFloat(TAG_PROJECTILE_DAMAGE)) : Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponDamage);
    }

    private boolean canDamageEntity(Entity entity) {
        ResourceLocation entityId = EntityList.getKey(entity);
        return StandAndHoldConfig.isConfiguredParasiteEntity(entityId);
    }
}
