package com.liamryan.standandhold.common.util;

import com.liamryan.standandhold.common.item.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public final class ParasiteSampleHelper {
    private ParasiteSampleHelper() {
    }

    public static int getAvailableParasiteSamples(@Nullable EntityPlayer player) {
        if (player == null) {
            return 0;
        }

        if (player.capabilities.isCreativeMode) {
            return Integer.MAX_VALUE;
        }

        return countParasiteSamples(player);
    }

    public static int countParasiteSamples(EntityPlayer player) {
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == ModItems.PARASITE_TISSUE_SAMPLE) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void consumeParasiteSamples(@Nullable EntityPlayer player, int sampleCost) {
        if (player == null || sampleCost <= 0 || player.capabilities.isCreativeMode) {
            return;
        }

        int remaining = sampleCost;
        for (int i = 0; i < player.inventory.mainInventory.size() && remaining > 0; i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (stack.isEmpty() || stack.getItem() != ModItems.PARASITE_TISSUE_SAMPLE) {
                continue;
            }

            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
        }
        player.inventory.markDirty();
    }
}
