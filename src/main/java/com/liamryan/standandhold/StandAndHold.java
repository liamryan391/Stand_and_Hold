package com.liamryan.standandhold;

import com.liamryan.standandhold.common.command.CommandStandAndHold;
import com.liamryan.standandhold.common.entity.ModEntities;
import com.liamryan.standandhold.common.event.HumanProgressionEventHandler;
import com.liamryan.standandhold.common.gui.StandAndHoldGuiHandler;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import com.liamryan.standandhold.common.proxy.CommonProxy;
import com.liamryan.standandhold.common.worldgen.ArmyCheckpointWorldGenerator;
import com.liamryan.standandhold.common.worldgen.MainBaseWorldGenerator;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
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

    @SidedProxy(
            clientSide = "com.liamryan.standandhold.client.ClientProxy",
            serverSide = "com.liamryan.standandhold.common.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        StandAndHoldConfig.sync();
        ModEntities.registerEntities();
        GameRegistry.registerWorldGenerator(new ArmyCheckpointWorldGenerator(), 0);
        GameRegistry.registerWorldGenerator(new MainBaseWorldGenerator(), 1);
        GameRegistry.registerTileEntity(TileEntityFieldCommandPost.class, StandAndHoldConstants.MOD_ID + ":field_command_post");
        GameRegistry.registerTileEntity(TileEntityResearchLab.class, StandAndHoldConstants.MOD_ID + ":research_lab");
        proxy.preInit(event);
        LOGGER.info("{} {} pre-initialized.", StandAndHoldConstants.MOD_NAME, StandAndHoldConstants.VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new HumanProgressionEventHandler());
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new StandAndHoldGuiHandler());
        LOGGER.info("{} initialized.", StandAndHoldConstants.MOD_NAME);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        HumanProgressionEventHandler.logOptionalCompatibilityState();
        LOGGER.info("{} post-initialized.", StandAndHoldConstants.MOD_NAME);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandStandAndHold());
        LOGGER.info("Registered Stand and Hold server commands.");
    }
}
