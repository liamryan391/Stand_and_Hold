package com.liamryan.standandhold.config;

import com.liamryan.standandhold.StandAndHoldConstants;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

@Config(modid = StandAndHoldConstants.MOD_ID, name = StandAndHoldConstants.MOD_ID)
public final class StandAndHoldConfig {
    @Config.Name("Debug Logging")
    @Config.Comment("Enables extra Stand and Hold log messages while developing the mod.")
    public static boolean debugLogging = false;

    private StandAndHoldConfig() {
    }

    public static void sync() {
        ConfigManager.sync(StandAndHoldConstants.MOD_ID, Config.Type.INSTANCE);
    }
}
