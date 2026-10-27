package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CommandStandAndHold {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(
            message -> Component.literal(String.valueOf(message)));

    private CommandStandAndHold() {
    }

    public static void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("standandhold")
                .executes(context -> status(context.getSource()))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("addpoints")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(context -> respond(context.getSource(),
                                        () -> HumanPointManager.addPoints(context.getSource().getServer(),
                                                LongArgumentType.getLong(context, "amount")), true))))
                .then(Commands.literal("setstage")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("stage", IntegerArgumentType.integer(0, 6))
                                .executes(context -> respond(context.getSource(),
                                        () -> HumanPointManager.setStage(context.getSource().getServer(),
                                                HumanStage.fromLevel(IntegerArgumentType.getInteger(context, "stage"))), true))));
        CommandFoundation.addChildren(root);
        CommandBuildings.addChildren(root);
        CommandStructures.addChildren(root);
        CommandResponse.addChildren(root);
        CommandCompatibility.addChildren(root);
        CommandGuide.addChildren(root);
        CommandSupport.addChildren(root);
        dispatcher.register(root);
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        return respond(source, () -> HumanPointManager.status(source.getServer()), false);
    }

    private static int respond(CommandSourceStack source, Supplier<HumanPointManager.Progression> operation,
                               boolean broadcast) throws CommandSyntaxException {
        try {
            HumanPointManager.Progression state = operation.get();
            source.sendSuccess(() -> Component.literal("Human points: " + state.points() + " | Stage "
                    + state.stage().level() + ": " + state.stage().displayName()), broadcast);
            return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw ERROR.create(exception.getMessage());
        }
    }
}
