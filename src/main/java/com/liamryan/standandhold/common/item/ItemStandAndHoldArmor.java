package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.equipment.EquipmentUnlockManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class ItemStandAndHoldArmor extends ItemArmor {
    private final String armorTextureBase;

    public ItemStandAndHoldArmor(String registryName, ArmorMaterial material, EntityEquipmentSlot equipmentSlot, String armorTextureBase) {
        super(material, 0, equipmentSlot);
        this.armorTextureBase = armorTextureBase;
        setRegistryName(StandAndHoldConstants.MOD_ID, registryName);
        setTranslationKey(StandAndHoldConstants.MOD_ID + "." + registryName);
        setCreativeTab(ModItems.CREATIVE_TAB);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        String layer = slot == EntityEquipmentSlot.LEGS ? "_layer_2.png" : "_layer_1.png";
        return armorTextureBase + layer;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (EquipmentUnlockManager.isLockedForPlayer(player, stack)) {
            EquipmentUnlockManager.sendLockedMessage(player, stack);
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }
}
