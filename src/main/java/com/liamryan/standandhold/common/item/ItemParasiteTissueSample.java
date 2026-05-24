package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.item.Item;

public final class ItemParasiteTissueSample extends Item {
    public ItemParasiteTissueSample() {
        setRegistryName(StandAndHoldConstants.MOD_ID, "parasite_tissue_sample");
        setTranslationKey(StandAndHoldConstants.MOD_ID + ".parasite_tissue_sample");
        setCreativeTab(ModItems.CREATIVE_TAB);
    }
}
