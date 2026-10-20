package com.liamryan.standandhold.common.sample;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Only the 36 main slots and offhand are consumed; withdrawals use the 36 main slots. */
public final class SampleInventory {
    private SampleInventory() { }
    public static int count(Inventory inventory) {
        int count = 0;
        for (var stack : inventory.getNonEquipmentItems()) { if (stack.is(SampleItems.TISSUE.get())) { count += stack.getCount(); } }
        ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
        return count + (offhand.is(SampleItems.TISSUE.get()) ? offhand.getCount() : 0);
    }
    public static int room(Inventory inventory) {
        ItemStack sample = SampleItems.TISSUE.get().getDefaultInstance();
        int room = 0;
        for (var stack : inventory.getNonEquipmentItems()) {
            if (stack.isEmpty()) { room += sample.getMaxStackSize(); }
            else if (ItemStack.isSameItemSameComponents(stack, sample)) { room += Math.max(0, stack.getMaxStackSize() - stack.getCount()); }
        }
        return room;
    }
    public static void transfer(ServerPlayer player, BuildingBlockEntity building, boolean deposit) {
        if (!player.level().getServer().isSameThread()) { throw new IllegalStateException("Sample transfers require the server thread."); }
        building.requireWritable();
        BuildingState state = building.state();
        Inventory inventory = player.getInventory();
        int amount = deposit ? SampleTransferRules.deposit(count(inventory), state.samples()) : SampleTransferRules.withdraw(room(inventory), state.samples());
        BuildingState next = deposit ? state.insertSamples(amount)
                : new BuildingState(state.level(), state.supplies(), state.samples() - amount, state.target(), state.work(), state.researching());
        int remaining = amount;
        ItemStack sample = SampleItems.TISSUE.get().getDefaultInstance();
        // All capacity/schema checks precede mutations; no creative-mode add() shortcut may discard a full stack.
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (deposit && stack.is(SampleItems.TISSUE.get())) {
                int move = Math.min(remaining, stack.getCount()); stack.shrink(move); remaining -= move;
            } else if (!deposit && (stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, sample))) {
                int move = Math.min(remaining, stack.isEmpty() ? sample.getMaxStackSize() : Math.max(0, stack.getMaxStackSize() - stack.getCount()));
                if (stack.isEmpty()) { inventory.setItem(slot, sample.copyWithCount(move)); } else { stack.grow(move); }
                remaining -= move;
            }
        }
        if (deposit && remaining > 0) { inventory.getItem(Inventory.SLOT_OFFHAND).shrink(remaining); }
        building.change(next);
        inventory.setChanged();
        player.inventoryMenu.broadcastChanges();
    }
}
