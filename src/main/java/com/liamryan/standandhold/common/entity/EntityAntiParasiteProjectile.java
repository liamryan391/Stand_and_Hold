package com.liamryan.standandhold.common.entity;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
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
                float damage = projectileDamage * Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponParasiteDamageMultiplier);
                hitEntity.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower == null ? this : thrower), damage);
                world.setEntityState(this, (byte) 3);
                if (StandAndHoldConfig.equipment.enablePrototypeProjectileHitSound) {
                    world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_IRONGOLEM_HURT, SoundCategory.NEUTRAL, 0.35F, 1.6F);
                }
            }
            setDead();
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id != 3) {
            super.handleStatusUpdate(id);
            return;
        }

        int particleCount = Math.max(0, StandAndHoldConfig.equipment.prototypeProjectileHitParticles);
        for (int i = 0; i < particleCount; i++) {
            world.spawnParticle(
                    EnumParticleTypes.CRIT,
                    posX,
                    posY,
                    posZ,
                    (rand.nextDouble() - 0.5D) * 0.2D,
                    (rand.nextDouble() - 0.5D) * 0.2D,
                    (rand.nextDouble() - 0.5D) * 0.2D
            );
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
