package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.config.EquipmentConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public final class EquipmentEvents {
    private EquipmentEvents() { }
    public static void attributes(ItemAttributeModifierEvent event) {
        if (!(event.getItemStack().getItem() instanceof EquipmentItem item)) { return; }
        if (item.kind == EquipmentKind.BLADE) {
            event.replaceModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                    EquipmentConfig.value(EquipmentConfig.BLADE_DAMAGE) - 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        } else if (item.armorType != null) {
            int piece = EquipmentRegistry.ARMOR_TYPES.indexOf(item.armorType);
            if (piece < 0) { return; }
            Identifier id = Identifier.withDefaultNamespace("armor." + item.armorType.getName());
            var slot = EquipmentSlotGroup.bySlot(item.armorType.getSlot());
            event.replaceModifier(Attributes.ARMOR, new AttributeModifier(id, EquipmentConfig.defense(item.kind, piece), AttributeModifier.Operation.ADD_VALUE), slot);
            event.replaceModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, EquipmentConfig.value(item.kind == EquipmentKind.ELITE
                    ? EquipmentConfig.ELITE_TOUGHNESS : EquipmentConfig.ARMY_TOUGHNESS), AttributeModifier.Operation.ADD_VALUE), slot);
        }
    }
    public static void attack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || !(player.getMainHandItem().getItem() instanceof EquipmentItem item)
                || item.kind != EquipmentKind.BLADE && item.kind != EquipmentKind.PROTOTYPE) { return; }
        boolean allowed = EquipmentAccess.allowed(level, item.kind);
        if (!allowed || item.kind == EquipmentKind.PROTOTYPE || item.kind == EquipmentKind.BLADE
                && (event.getTarget() instanceof Player || event.getTarget() instanceof HumanNpc)) { event.setCanceled(true); }
        if (!allowed) { EquipmentAccess.notice(player, item.kind); }
    }
    public static void damage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !(event.getSource().getEntity() instanceof LivingEntity owner)
                || event.getSource().getDirectEntity() != owner || !(event.getSource().is(DamageTypes.PLAYER_ATTACK) || event.getSource().is(DamageTypes.MOB_ATTACK))
                || !(owner.getMainHandItem().getItem() instanceof EquipmentItem item) || item.kind != EquipmentKind.BLADE) { return; }
        if (!EquipmentAccess.allowed(level, item.kind) || event.getEntity() instanceof Player || event.getEntity() instanceof HumanNpc || owner.isAlliedTo(event.getEntity())) {
            event.setCanceled(true); return;
        }
        if (EquipmentAccess.mapped(event.getEntity())) { event.setAmount(EquipmentRules.damage(event.getAmount() * EquipmentConfig.value(EquipmentConfig.THREAT_MULTIPLIER))); }
    }
}
