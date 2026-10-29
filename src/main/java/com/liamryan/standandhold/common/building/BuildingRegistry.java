package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.sample.SampleItems;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BuildingRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StandAndHoldConstants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StandAndHoldConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StandAndHoldConstants.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, StandAndHoldConstants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StandAndHoldConstants.MOD_ID);
    public static final DeferredBlock<BuildingBlock> COMMAND_POST = block(BuildingKind.COMMAND_POST);
    public static final DeferredBlock<BuildingBlock> LAB = block(BuildingKind.LAB);
    public static final DeferredBlock<BuildingBlock> CRATE = block(BuildingKind.CRATE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BuildingBlockEntity>> ENTITY = ENTITIES.register("building",
            () -> new BlockEntityType<>(BuildingBlockEntity::new, true, COMMAND_POST.get(), LAB.get(), CRATE.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<BuildingMenu>> MENU = MENUS.register("building", () -> IMenuTypeExtension.create(BuildingMenu::new));
    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("buildings", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.standandhold"))
            .icon(() -> COMMAND_POST.asItem().getDefaultInstance()).displayItems((parameters, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                output.accept(SampleItems.TISSUE.get());
                output.accept(NpcRegistry.SURVIVOR_EGG.get());
                output.accept(NpcRegistry.ARMY_EGG.get());
                output.accept(NpcRegistry.HEAVY_EGG.get());
                output.accept(NpcRegistry.ELITE_EGG.get());
                com.liamryan.standandhold.common.equipment.EquipmentRegistry.creativeItems(output::accept);
            }).build());
    private BuildingRegistry() { }
    private static DeferredBlock<BuildingBlock> block(BuildingKind kind) {
        var block = BLOCKS.registerBlock(kind.id(), properties -> new BuildingBlock(kind, properties),
                () -> BlockBehaviour.Properties.of().strength(3.0F, 6.0F).pushReaction(PushReaction.IMMOVEABLE));
        ITEMS.registerSimpleBlockItem(block);
        return block;
    }
    public static void register(IEventBus bus) { BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); MENUS.register(bus); TABS.register(bus); }
}
