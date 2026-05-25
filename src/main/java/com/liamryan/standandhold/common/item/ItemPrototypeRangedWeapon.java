package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.entity.EntityAntiParasiteProjectile;
import com.liamryan.standandhold.common.equipment.EquipmentUnlockManager;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.init.SoundEvents;

public final class ItemPrototypeRangedWeapon extends Item {
    public ItemPrototypeRangedWeapon() {
        setRegistryName(StandAndHoldConstants.MOD_ID, "prototype_ranged_weapon");
        setTranslationKey(StandAndHoldConstants.MOD_ID + ".prototype_ranged_weapon");
        setCreativeTab(ModItems.CREATIVE_TAB);
        setMaxStackSize(1);
        setMaxDamage(Math.max(1, StandAndHoldConfig.equipment.prototypeRangedWeaponMaxUses));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (EquipmentUnlockManager.isLockedForPlayer(player, stack)) {
            EquipmentUnlockManager.sendLockedMessage(player, stack);
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        if (player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        if (!world.isRemote) {
            EntityAntiParasiteProjectile projectile = createProjectile(world, player, Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponDamage));
            projectile.shoot(
                    player,
                    player.rotationPitch,
                    player.rotationYaw,
                    0.0F,
                    Math.max(0.1F, StandAndHoldConfig.equipment.prototypeRangedWeaponVelocity),
                    Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponInaccuracy)
            );
            world.spawnEntity(projectile);
            stack.damageItem(1, player);
        }

        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS,
                0.5F,
                1.2F / (itemRand.nextFloat() * 0.4F + 0.8F)
        );
        player.getCooldownTracker().setCooldown(this, Math.max(1, StandAndHoldConfig.equipment.prototypeRangedWeaponCooldownTicks));
        player.swingArm(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    public static void fireFromNpc(World world, EntityLivingBase shooter, EntityLivingBase target) {
        if (world == null || world.isRemote || shooter == null || target == null) {
            return;
        }

        EntityAntiParasiteProjectile projectile = createProjectile(world, shooter, Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponDamage));
        double deltaX = target.posX - shooter.posX;
        double deltaY = target.getEntityBoundingBox().minY + (double) (target.height * 0.5F) - projectile.posY;
        double deltaZ = target.posZ - shooter.posZ;
        double horizontalDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        projectile.shoot(
                deltaX,
                deltaY + horizontalDistance * 0.05D,
                deltaZ,
                Math.max(0.1F, StandAndHoldConfig.equipment.prototypeRangedWeaponVelocity),
                Math.max(0.0F, StandAndHoldConfig.equipment.prototypeRangedWeaponInaccuracy)
        );
        world.spawnEntity(projectile);
        world.playSound(
                null,
                shooter.posX,
                shooter.posY,
                shooter.posZ,
                SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.HOSTILE,
                0.5F,
                1.0F
        );
    }

    private static EntityAntiParasiteProjectile createProjectile(World world, EntityLivingBase shooter, float projectileDamage) {
        return new EntityAntiParasiteProjectile(world, shooter, projectileDamage);
    }
}
