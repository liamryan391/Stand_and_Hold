package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingPositions;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

final class CommandBuildings {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(value -> Component.literal(String.valueOf(value)));
    private CommandBuildings() { }
    static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("commandpost").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list").executes(context -> inspect(context.getSource(), false)))
                .then(Commands.literal("nearest").executes(context -> inspect(context.getSource(), true))));
    }
    private static int inspect(CommandSourceStack source, boolean nearest) throws CommandSyntaxException {
        try {
            HumanWorldData data = HumanWorldData.get(source.getServer());
            int cleaned = BuildingPositions.cleanLoaded(source.getLevel(), data);
            var positions = data.commandPostKeys().stream().limit(4096).map(BuildingPosition::parse).flatMap(java.util.Optional::stream)
                    .filter(position -> !nearest || position.dimension().equals(source.getLevel().dimension().identifier()))
                    .sorted(Comparator.comparingDouble(position -> position.pos().distToCenterSqr(source.getPosition())))
                    .limit(nearest ? 1 : 20).toList();
            source.sendSuccess(() -> Component.literal("Registered Field Command Posts: " + data.commandPostKeys().size()
                    + "; cleaned " + cleaned + " stale records. Showing " + positions.size() + (nearest ? " nearest in this dimension." : " (maximum 20).")), false);
            for (var position : positions) {
                boolean loaded = position.dimension().equals(source.getLevel().dimension().identifier()) && source.getLevel().hasChunkAt(position.pos());
                source.sendSuccess(() -> Component.literal(position.key() + (loaded ? " [loaded]" : " [not inspected/unloaded]")), false);
            }
            return positions.size();
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
}
