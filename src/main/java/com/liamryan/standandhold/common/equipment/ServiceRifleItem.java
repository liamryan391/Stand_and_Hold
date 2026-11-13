package com.liamryan.standandhold.common.equipment;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * M35 interim service rifle: the Army Rifleman's issued weapon until the Roadmap 6 weapon framework. Soldiers fire it
 * through {@code HumanNpc}; players can hold it but not fire it yet, and soldiers never drop it.
 */
public final class ServiceRifleItem extends Item {
    public ServiceRifleItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, output, flag);
        output.accept(Component.translatable("tooltip.standandhold.service_rifle"));
        output.accept(Component.translatable("tooltip.standandhold.service_rifle_players"));
    }
}
