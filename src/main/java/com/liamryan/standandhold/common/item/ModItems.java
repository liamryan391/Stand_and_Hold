package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = StandAndHoldConstants.MOD_ID)
public final class ModItems {
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(StandAndHoldConstants.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(PARASITE_TISSUE_SAMPLE);
        }
    };

    public static final Item PARASITE_TISSUE_SAMPLE = new ItemParasiteTissueSample();

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(PARASITE_TISSUE_SAMPLE);
    }
}
