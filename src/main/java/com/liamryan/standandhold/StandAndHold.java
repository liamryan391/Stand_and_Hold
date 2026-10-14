package com.liamryan.standandhold;

import com.liamryan.standandhold.config.StandAndHoldConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(StandAndHoldConstants.MOD_ID)
public final class StandAndHold {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String version;

    public StandAndHold(IEventBus modEventBus, ModContainer modContainer) {
        version = modContainer.getModInfo().getVersion().toString();
        modContainer.registerConfig(ModConfig.Type.LOCAL, StandAndHoldConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (StandAndHoldConfig.LOG_STARTUP.getAsBoolean()) {
            LOGGER.info("{} {} initialized: modern NeoForge foundation (no gameplay yet).",
                    StandAndHoldConstants.MOD_NAME, version);
        }
    }
}
