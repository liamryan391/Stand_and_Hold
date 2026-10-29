package com.liamryan.standandhold.common.npc;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NpcRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, StandAndHoldConstants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StandAndHoldConstants.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanNpc>> SURVIVOR = type(HumanTier.SURVIVOR_DEFENDER);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanNpc>> ARMY = type(HumanTier.ARMY_RIFLEMAN);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanNpc>> HEAVY = type(HumanTier.HEAVY_SOLDIER);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanNpc>> ELITE = type(HumanTier.ELITE_SOLDIER);
    public static final DeferredItem<SpawnEggItem> SURVIVOR_EGG = egg(HumanTier.SURVIVOR_DEFENDER, SURVIVOR);
    public static final DeferredItem<SpawnEggItem> ARMY_EGG = egg(HumanTier.ARMY_RIFLEMAN, ARMY);
    public static final DeferredItem<SpawnEggItem> HEAVY_EGG = egg(HumanTier.HEAVY_SOLDIER, HEAVY);
    public static final DeferredItem<SpawnEggItem> ELITE_EGG = egg(HumanTier.ELITE_SOLDIER, ELITE);
    private NpcRegistry() { }
    public static EntityType<HumanNpc> entityType(HumanTier tier) {
        return switch (tier) {
            case SURVIVOR_DEFENDER -> SURVIVOR.get();
            case ARMY_RIFLEMAN -> ARMY.get();
            case HEAVY_SOLDIER -> HEAVY.get();
            case ELITE_SOLDIER -> ELITE.get();
        };
    }
    public static HumanTier tier(EntityType<?> type) {
        for (HumanTier tier : HumanTier.values()) { if (entityType(tier) == type) { return tier; } }
        throw new IllegalArgumentException("Not a registered human NPC type");
    }
    private static DeferredHolder<EntityType<?>, EntityType<HumanNpc>> type(HumanTier tier) {
        return ENTITIES.register(tier.id, id -> EntityType.Builder.of(HumanNpc::new, MobCategory.MISC)
                .sized(0.6F, 1.8F).eyeHeight(1.62F).clientTrackingRange(8)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }
    private static DeferredItem<SpawnEggItem> egg(HumanTier tier, DeferredHolder<EntityType<?>, EntityType<HumanNpc>> type) {
        return ITEMS.registerItem(tier.id + "_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(type.get())));
    }
    public static void register(IEventBus bus) {
        ENTITIES.register(bus); ITEMS.register(bus); bus.addListener(NpcRegistry::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        for (HumanTier tier : HumanTier.values()) { event.put(entityType(tier), HumanNpc.attributes(tier).build()); }
    }
}
