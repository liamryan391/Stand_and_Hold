package com.liamryan.standandhold.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class StandAndHoldConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_STARTUP = BUILDER
            .comment("Log the mod version once during common setup. No gameplay settings exist yet.")
            .define("logStartup", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private StandAndHoldConfig() {
    }
}
