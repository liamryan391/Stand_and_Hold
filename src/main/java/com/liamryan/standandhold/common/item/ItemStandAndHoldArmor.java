package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

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
}
