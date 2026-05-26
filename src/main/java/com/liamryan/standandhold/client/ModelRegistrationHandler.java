package com.liamryan.standandhold.client;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.block.ModBlocks;
import com.liamryan.standandhold.common.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = StandAndHoldConstants.MOD_ID, value = Side.CLIENT)
public final class ModelRegistrationHandler {
    private ModelRegistrationHandler() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerItemModel(ModItems.PARASITE_TISSUE_SAMPLE);
        registerItemModel(ModItems.ARMY_HELMET);
        registerItemModel(ModItems.ARMY_CHESTPLATE);
        registerItemModel(ModItems.ARMY_LEGGINGS);
        registerItemModel(ModItems.ARMY_BOOTS);
        registerItemModel(ModItems.ELITE_HELMET);
        registerItemModel(ModItems.ELITE_CHESTPLATE);
        registerItemModel(ModItems.ELITE_LEGGINGS);
        registerItemModel(ModItems.ELITE_BOOTS);
        registerItemModel(ModItems.SPECIAL_DIVISION_HELMET);
        registerItemModel(ModItems.SPECIAL_DIVISION_CHESTPLATE);
        registerItemModel(ModItems.SPECIAL_DIVISION_LEGGINGS);
        registerItemModel(ModItems.SPECIAL_DIVISION_BOOTS);
        registerItemModel(ModItems.ANTI_PARASITE_BLADE);
        registerItemModel(ModItems.PROTOTYPE_RANGED_WEAPON);
        registerItemModel(Item.getItemFromBlock(ModBlocks.FIELD_COMMAND_POST));
        registerItemModel(Item.getItemFromBlock(ModBlocks.RESEARCH_LAB));
        registerItemModel(Item.getItemFromBlock(ModBlocks.SUPPLY_CRATE));
    }

    private static void registerItemModel(Item item) {
        if (item == null || item.getRegistryName() == null) {
            return;
        }

        ResourceLocation registryName = item.getRegistryName();
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
    }
}
