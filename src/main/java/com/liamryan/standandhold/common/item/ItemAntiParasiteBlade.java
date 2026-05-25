package com.liamryan.standandhold.common.item;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.item.ItemSword;

public final class ItemAntiParasiteBlade extends ItemSword {
    public ItemAntiParasiteBlade(ToolMaterial material) {
        super(material);
        setRegistryName(StandAndHoldConstants.MOD_ID, "anti_parasite_blade");
        setTranslationKey(StandAndHoldConstants.MOD_ID + ".anti_parasite_blade");
        setCreativeTab(ModItems.CREATIVE_TAB);
    }
}
