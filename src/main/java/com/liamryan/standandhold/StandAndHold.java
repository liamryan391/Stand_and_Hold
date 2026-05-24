package com.liamryan.standandhold;

import com.liamryan.standandhold.common.command.CommandStandAndHold;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = StandAndHoldConstants.MOD_ID,
        name = StandAndHoldConstants.MOD_NAME,
        version = StandAndHoldConstants.VERSION,
        acceptedMinecraftVersions = StandAndHoldConstants.ACCEPTED_MINECRAFT_VERSIONS,
        dependencies = StandAndHoldConstants.DEPENDENCIES
)
public final class StandAndHold {
    public static final Logger LOGGER = LogManager.getLogger(StandAndHoldConstants.MOD_NAME);

    @Mod.Instance(StandAndHoldConstants.MOD_ID)
    public static StandAndHold instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        StandAndHoldConfig.sync();
        LOGGER.info("{} {} pre-initialized.", StandAndHoldConstants.MOD_NAME, StandAndHoldConstants.VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} initialized.", StandAndHoldConstants.MOD_NAME);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("{} post-initialized.", StandAndHoldConstants.MOD_NAME);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandStandAndHold());
        LOGGER.info("Registered Stand and Hold server commands.");
    }
}
