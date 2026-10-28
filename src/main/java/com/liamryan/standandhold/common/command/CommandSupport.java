package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.config.ResponseConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

final class CommandSupport {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(value -> Component.literal(String.valueOf(value)));
    private CommandSupport() { }
    static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("support")
                .then(Commands.literal("status").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("Support call-ins " + (ResponseConfig.SUPPORT.get() ? "enabled" : "disabled")
                            + ": Stage " + ResponseConfig.SUPPORT_STAGE.get() + ", research " + ResponseConfig.SUPPORT_RESEARCH.get()
                            + ", " + ResponseConfig.SUPPORT_COST.get() + " global supplies. Look at a loaded Command Post within 6 blocks; shared defender caps/cooldown apply."), false);
                    return 1;
                }))
                .then(Commands.literal("reinforce").executes(context -> {
                    try {
                        var player = context.getSource().getPlayerOrException();
                        var result = SupportCallIn.request(player, SupportCallIn.lookedAt(player));
                        context.getSource().sendSuccess(() -> Component.literal(result.message()), false);
                        return result.success() ? 1 : 0;
                    } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
                })));
    }
}
