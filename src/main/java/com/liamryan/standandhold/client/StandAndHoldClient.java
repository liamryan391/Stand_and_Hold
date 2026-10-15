package com.liamryan.standandhold.client;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.building.BuildingRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = StandAndHoldConstants.MOD_ID, dist = Dist.CLIENT)
public final class StandAndHoldClient {
    public StandAndHoldClient(IEventBus bus) { bus.addListener(StandAndHoldClient::screens); }
    private static void screens(RegisterMenuScreensEvent event) { event.register(BuildingRegistry.MENU.get(), BuildingScreen::new); }
}
