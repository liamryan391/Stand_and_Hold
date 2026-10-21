package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.StandAndHoldConstants;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EquipmentRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StandAndHoldConstants.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, StandAndHoldConstants.MOD_ID);
    public static final List<ArmorType> ARMOR_TYPES = List.of(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS);
    public static final DeferredItem<EquipmentItem> BLADE = ITEMS.registerItem("anti_threat_blade",
            properties -> new EquipmentItem(properties.sword(ToolMaterial.IRON, 3, -2.4F), EquipmentKind.BLADE, null));
    public static final List<DeferredItem<EquipmentItem>> ARMY = armor(EquipmentKind.ARMY);
    public static final List<DeferredItem<EquipmentItem>> ELITE = armor(EquipmentKind.ELITE);
    public static final DeferredItem<PrototypeLauncherItem> PROTOTYPE = ITEMS.registerItem("prototype_launcher", properties -> new PrototypeLauncherItem(properties.durability(256).repairable(net.minecraft.world.item.Items.IRON_INGOT)));
    public static final DeferredItem<Item> BOLT = ITEMS.registerSimpleItem("prototype_bolt");
    public static final DeferredHolder<EntityType<?>, EntityType<PrototypeBolt>> PROJECTILE = ENTITIES.register("prototype_bolt", id -> EntityType.Builder.<PrototypeBolt>of(PrototypeBolt::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    private EquipmentRegistry() { }
    private static List<DeferredItem<EquipmentItem>> armor(EquipmentKind kind) {
        boolean elite = kind == EquipmentKind.ELITE;
        ArmorMaterial material = new ArmorMaterial(elite ? 25 : 15, Map.of(ArmorType.HELMET, elite ? 3 : 2, ArmorType.CHESTPLATE, elite ? 7 : 6,
                ArmorType.LEGGINGS, elite ? 6 : 5, ArmorType.BOOTS, elite ? 3 : 2), 10, SoundEvents.ARMOR_EQUIP_IRON,
                elite ? 1 : 0, 0, TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "repairs_equipment")),
                ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, kind.id)));
        return ARMOR_TYPES.stream().map(type -> ITEMS.registerItem(kind.id + "_" + type.getName(), properties -> new EquipmentItem(
                properties.humanoidArmor(material, type).component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot())
                        .setEquipSound(material.equipSound()).setAsset(material.assetId()).setDispensable(false).build()), kind, type))).toList();
    }
    public static void register(IEventBus bus) { ITEMS.register(bus); ENTITIES.register(bus); }
    public static void creativeItems(java.util.function.Consumer<Item> output) {
        output.accept(BLADE.get()); ARMY.forEach(item -> output.accept(item.get())); ELITE.forEach(item -> output.accept(item.get())); output.accept(PROTOTYPE.get());
    }
}
