package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.block.ModBlocks;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = StandAndHoldConstants.MOD_ID)
public final class ModItems {
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(StandAndHoldConstants.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.FIELD_COMMAND_POST);
        }
    };

    public static final Item PARASITE_TISSUE_SAMPLE = new ItemParasiteTissueSample();
    private static final ItemArmor.ArmorMaterial ARMY_ARMOR_MATERIAL = createArmorMaterial(
            "standandhold_army",
            StandAndHoldConfig.equipment.armyArmorDurability,
            StandAndHoldConfig.equipment.armyArmorReductions,
            StandAndHoldConfig.equipment.armyArmorEnchantability,
            StandAndHoldConfig.equipment.armyArmorToughness
    );
    private static final ItemArmor.ArmorMaterial ELITE_ARMOR_MATERIAL = createArmorMaterial(
            "standandhold_elite",
            StandAndHoldConfig.equipment.eliteArmorDurability,
            StandAndHoldConfig.equipment.eliteArmorReductions,
            StandAndHoldConfig.equipment.eliteArmorEnchantability,
            StandAndHoldConfig.equipment.eliteArmorToughness
    );
    private static final ItemArmor.ArmorMaterial SPECIAL_DIVISION_ARMOR_MATERIAL = createArmorMaterial(
            "standandhold_special_division",
            StandAndHoldConfig.equipment.specialDivisionArmorDurability,
            StandAndHoldConfig.equipment.specialDivisionArmorReductions,
            StandAndHoldConfig.equipment.specialDivisionArmorEnchantability,
            StandAndHoldConfig.equipment.specialDivisionArmorToughness
    );
    private static final ToolMaterial ANTI_PARASITE_BLADE_MATERIAL = EnumHelper.addToolMaterial(
            "standandhold_anti_parasite_blade",
            Math.max(0, StandAndHoldConfig.equipment.antiParasiteBladeHarvestLevel),
            Math.max(1, StandAndHoldConfig.equipment.antiParasiteBladeMaxUses),
            Math.max(0.0F, StandAndHoldConfig.equipment.antiParasiteBladeEfficiency),
            Math.max(0.0F, StandAndHoldConfig.equipment.antiParasiteBladeAttackDamage),
            Math.max(0, StandAndHoldConfig.equipment.antiParasiteBladeEnchantability)
    );

    public static final Item ARMY_HELMET = new ItemStandAndHoldArmor("army_helmet", ARMY_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD, "minecraft:textures/models/armor/iron");
    public static final Item ARMY_CHESTPLATE = new ItemStandAndHoldArmor("army_chestplate", ARMY_ARMOR_MATERIAL, EntityEquipmentSlot.CHEST, "minecraft:textures/models/armor/iron");
    public static final Item ARMY_LEGGINGS = new ItemStandAndHoldArmor("army_leggings", ARMY_ARMOR_MATERIAL, EntityEquipmentSlot.LEGS, "minecraft:textures/models/armor/iron");
    public static final Item ARMY_BOOTS = new ItemStandAndHoldArmor("army_boots", ARMY_ARMOR_MATERIAL, EntityEquipmentSlot.FEET, "minecraft:textures/models/armor/iron");

    public static final Item ELITE_HELMET = new ItemStandAndHoldArmor("elite_helmet", ELITE_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD, "minecraft:textures/models/armor/diamond");
    public static final Item ELITE_CHESTPLATE = new ItemStandAndHoldArmor("elite_chestplate", ELITE_ARMOR_MATERIAL, EntityEquipmentSlot.CHEST, "minecraft:textures/models/armor/diamond");
    public static final Item ELITE_LEGGINGS = new ItemStandAndHoldArmor("elite_leggings", ELITE_ARMOR_MATERIAL, EntityEquipmentSlot.LEGS, "minecraft:textures/models/armor/diamond");
    public static final Item ELITE_BOOTS = new ItemStandAndHoldArmor("elite_boots", ELITE_ARMOR_MATERIAL, EntityEquipmentSlot.FEET, "minecraft:textures/models/armor/diamond");

    public static final Item SPECIAL_DIVISION_HELMET = new ItemStandAndHoldArmor("special_division_helmet", SPECIAL_DIVISION_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD, "minecraft:textures/models/armor/diamond");
    public static final Item SPECIAL_DIVISION_CHESTPLATE = new ItemStandAndHoldArmor("special_division_chestplate", SPECIAL_DIVISION_ARMOR_MATERIAL, EntityEquipmentSlot.CHEST, "minecraft:textures/models/armor/diamond");
    public static final Item SPECIAL_DIVISION_LEGGINGS = new ItemStandAndHoldArmor("special_division_leggings", SPECIAL_DIVISION_ARMOR_MATERIAL, EntityEquipmentSlot.LEGS, "minecraft:textures/models/armor/diamond");
    public static final Item SPECIAL_DIVISION_BOOTS = new ItemStandAndHoldArmor("special_division_boots", SPECIAL_DIVISION_ARMOR_MATERIAL, EntityEquipmentSlot.FEET, "minecraft:textures/models/armor/diamond");

    public static final Item ANTI_PARASITE_BLADE = new ItemAntiParasiteBlade(ANTI_PARASITE_BLADE_MATERIAL);

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                PARASITE_TISSUE_SAMPLE,
                ARMY_HELMET,
                ARMY_CHESTPLATE,
                ARMY_LEGGINGS,
                ARMY_BOOTS,
                ELITE_HELMET,
                ELITE_CHESTPLATE,
                ELITE_LEGGINGS,
                ELITE_BOOTS,
                SPECIAL_DIVISION_HELMET,
                SPECIAL_DIVISION_CHESTPLATE,
                SPECIAL_DIVISION_LEGGINGS,
                SPECIAL_DIVISION_BOOTS,
                ANTI_PARASITE_BLADE
        );
    }

    private static ItemArmor.ArmorMaterial createArmorMaterial(String materialName, int durability, int[] reductions, int enchantability, float toughness) {
        return EnumHelper.addArmorMaterial(
                materialName,
                StandAndHoldConstants.MOD_ID + ":" + materialName,
                Math.max(1, durability),
                sanitizeArmorReductions(reductions),
                Math.max(0, enchantability),
                SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                Math.max(0.0F, toughness)
        );
    }

    private static int[] sanitizeArmorReductions(int[] configuredReductions) {
        int[] reductions = new int[] {
                2,
                5,
                6,
                2
        };
        if (configuredReductions == null) {
            return reductions;
        }

        for (int i = 0; i < reductions.length && i < configuredReductions.length; i++) {
            reductions[i] = Math.max(0, Math.min(20, configuredReductions[i]));
        }
        return reductions;
    }
}
