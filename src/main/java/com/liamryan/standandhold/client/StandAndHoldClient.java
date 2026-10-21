package com.liamryan.standandhold.client;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.building.BuildingRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.liamryan.standandhold.common.npc.HumanTier;
import com.liamryan.standandhold.common.npc.NpcRegistry;

@Mod(value = StandAndHoldConstants.MOD_ID, dist = Dist.CLIENT)
public final class StandAndHoldClient {
    public StandAndHoldClient(IEventBus bus) {
        bus.addListener(StandAndHoldClient::screens);
        bus.addListener(StandAndHoldClient::renderers);
        bus.addListener(StandAndHoldClient::layers);
    }
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NpcRegistry.SURVIVOR.get(), context -> new HumanNpcRenderer(context, HumanTier.SURVIVOR_DEFENDER));
        event.registerEntityRenderer(NpcRegistry.ARMY.get(), context -> new HumanNpcRenderer(context, HumanTier.ARMY_RIFLEMAN));
        event.registerEntityRenderer(com.liamryan.standandhold.common.equipment.EquipmentRegistry.PROJECTILE.get(), context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context, 0.6F, true));
    }
    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (HumanTier tier : HumanTier.values()) { event.registerLayerDefinition(HumanNpcRenderer.layerId(tier), () -> HumanNpcRenderer.layer(tier)); }
    }
    private static void screens(RegisterMenuScreensEvent event) { event.register(BuildingRegistry.MENU.get(), BuildingScreen::new); }
}
