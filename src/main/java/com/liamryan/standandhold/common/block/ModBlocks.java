package com.liamryan.standandhold.common.block;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = StandAndHoldConstants.MOD_ID)
public final class ModBlocks {
    public static final Block FIELD_COMMAND_POST = new BlockFieldCommandPost();
    public static final Block RESEARCH_LAB = new BlockResearchLab();

    private ModBlocks() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(FIELD_COMMAND_POST);
        event.getRegistry().register(RESEARCH_LAB);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(FIELD_COMMAND_POST).setRegistryName(FIELD_COMMAND_POST.getRegistryName()));
        event.getRegistry().register(new ItemBlock(RESEARCH_LAB).setRegistryName(RESEARCH_LAB.getRegistryName()));
    }
}
