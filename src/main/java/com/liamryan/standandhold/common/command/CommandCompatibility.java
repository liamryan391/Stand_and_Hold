package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.compat.OptionalThreatCompat;
import com.liamryan.standandhold.config.ThreatConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CommandCompatibility {
    private CommandCompatibility() { }
    public static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("compat").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(context -> {
                    var source = context.getSource();
                    var resolved = ThreatConfig.resolved();
                    String version = OptionalThreatCompat.installedVersion("contagion");
                    source.sendSuccess(() -> Component.literal("Contagion: " + (version == null ? "not installed" : version)
                            + " | Active presets: " + (resolved.active().isEmpty() ? "none" : String.join(", ", resolved.active()))
                            + " | Resolved threat rules: " + resolved.rules().rules().size()), false);
                    for (String issue : resolved.rules().issues().stream().limit(8).toList()) { source.sendSuccess(() -> Component.literal(issue), false); }
                    if (resolved.rules().issues().size() > 8) { source.sendSuccess(() -> Component.literal("More mapping issues are recorded in the config-load log."), false); }
                    return 1;
                })));
    }
}
