package com.liamryan.standandhold.common.sample;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SampleItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StandAndHoldConstants.MOD_ID);
    public static final DeferredItem<Item> TISSUE = ITEMS.registerSimpleItem("parasite_tissue_sample", properties -> properties.stacksTo(64));
    private SampleItems() { }
    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
