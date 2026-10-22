package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.config.EquipmentConfig;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;

public class EquipmentItem extends Item {
    public final EquipmentKind kind;
    public final ArmorType armorType;
    public EquipmentItem(Properties properties, EquipmentKind kind, ArmorType armorType) {
        super(properties); this.kind = kind; this.armorType = armorType;
    }
    @Override public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return super.canEquip(stack, slot, entity) && (!(entity.level() instanceof ServerLevel) || EquipmentAccess.allowedFor(entity, kind));
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() && armorType != null) { return InteractionResult.SUCCESS; }
        if (level instanceof ServerLevel && !EquipmentAccess.allowedFor(player, kind)) {
            EquipmentAccess.notice(player, kind); return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, output, flag);
        var gate = EquipmentConfig.gate(kind);
        output.accept(Component.translatable(EquipmentConfig.value(EquipmentConfig.PLAYER_GATES) ? "tooltip.standandhold.equipment_gate" : "tooltip.standandhold.player_equipment_free",
                EquipmentConfig.value(gate.stage), EquipmentConfig.value(gate.research)));
        if (kind == EquipmentKind.PROTOTYPE) { output.accept(Component.translatable("tooltip.standandhold.npc_equipment_gate", EquipmentConfig.value(gate.stage), EquipmentConfig.value(gate.research))); }
        if (kind == EquipmentKind.BLADE || kind == EquipmentKind.PROTOTYPE) { output.accept(Component.translatable("tooltip.standandhold.friendly_safe")); }
    }
}
