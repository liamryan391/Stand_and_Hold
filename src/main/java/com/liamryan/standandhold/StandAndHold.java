package com.liamryan.standandhold;

import com.liamryan.standandhold.common.command.CommandStandAndHold;
import com.liamryan.standandhold.common.building.BuildingRegistry;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.sample.SampleItems;
import com.liamryan.standandhold.common.sample.SampleEvents;
import com.liamryan.standandhold.common.threat.ThreatEvents;
import com.liamryan.standandhold.config.ThreatConfig;
import com.liamryan.standandhold.common.npc.NpcRegistry;
import com.liamryan.standandhold.config.NpcConfig;
import com.liamryan.standandhold.config.BuildingConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.liamryan.standandhold.common.structure.ResistanceSites;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(StandAndHoldConstants.MOD_ID)
public final class StandAndHold {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String version;

    public StandAndHold(IEventBus modEventBus, ModContainer modContainer) {
        version = modContainer.getModInfo().getVersion().toString();
        modContainer.registerConfig(ModConfig.Type.LOCAL, StandAndHoldConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SYNCED, HumanProgressionConfig.SPEC, "standandhold-progression.toml");
        modContainer.registerConfig(ModConfig.Type.SYNCED, BuildingConfig.SPEC, "standandhold-buildings.toml");
        modContainer.registerConfig(ModConfig.Type.SYNCED, ThreatConfig.SPEC, "standandhold-threats.toml");
        modContainer.registerConfig(ModConfig.Type.SYNCED, NpcConfig.SPEC, "standandhold-npcs.toml");
        modContainer.registerConfig(ModConfig.Type.SYNCED, StructureConfig.SPEC, "standandhold-structures.toml");
        modEventBus.addListener((ModConfigEvent.Loading event) -> ThreatConfig.onConfig(event));
        modEventBus.addListener((ModConfigEvent.Reloading event) -> ThreatConfig.onConfig(event));
        SampleItems.register(modEventBus);
        NpcRegistry.register(modEventBus);
        BuildingRegistry.register(modEventBus);
        ResistanceSites.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(ThreatEvents::onDrops);
        NeoForge.EVENT_BUS.addListener(SampleEvents::onPickup);
        NeoForge.EVENT_BUS.addListener(FoundationResources::register);
        NeoForge.EVENT_BUS.addListener(CommandStandAndHold::onRegister);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (StandAndHoldConfig.LOG_STARTUP.getAsBoolean()) {
            LOGGER.info("{} {} initialized: modern NeoForge progression foundation.",
                    StandAndHoldConstants.MOD_NAME, version);
        }
    }
}
