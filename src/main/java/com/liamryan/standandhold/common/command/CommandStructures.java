package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingPositions;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.structure.StructureKind;
import com.liamryan.standandhold.common.structure.StructurePlacement;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

final class CommandStructures {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(value -> Component.literal(String.valueOf(value)));
    private CommandStructures() { }
    static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("structure").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("checkpoint").executes(context -> place(context.getSource(), StructureKind.CHECKPOINT)))
                .then(Commands.literal("mainbase").executes(context -> place(context.getSource(), StructureKind.MAIN_BASE))));
        root.then(Commands.literal("mainbase").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list").executes(context -> inspect(context.getSource(), false)))
                .then(Commands.literal("nearest").executes(context -> inspect(context.getSource(), true)))
                .then(Commands.literal("status").executes(context -> inspect(context.getSource(), true))));
    }
    private static int place(CommandSourceStack source, StructureKind kind) throws CommandSyntaxException {
        try {
            BlockPos origin = BlockPos.containing(source.getPosition());
            var result = StructurePlacement.place(source.getLevel(), kind, origin.getX() >> 4, origin.getZ() >> 4);
            if (!result.success()) { throw new IllegalArgumentException("Structure not placed: " + result.message()); }
            var data = HumanWorldData.get(source.getServer());
            source.sendSuccess(() -> Component.literal(result.message() + " Registered posts: " + data.commandPostKeys().size()
                    + "; Main Bases: " + data.mainBaseKeys().size() + "."), true);
            MissionManager.refresh(data, FoundationResources.get(source.getServer()), HumanProgressionConfig.thresholds())
                    .forEach(message -> source.sendSuccess(() -> Component.literal(message), false));
            return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
    private static int inspect(CommandSourceStack source, boolean nearest) throws CommandSyntaxException {
        try {
            var data = HumanWorldData.get(source.getServer());
            int cleaned = BuildingPositions.cleanLoaded(source.getLevel(), data);
            int stage = ProgressionRules.stageFor(data.points(), HumanProgressionConfig.thresholds()).level();
            var positions = data.mainBaseKeys().stream().limit(4096).map(BuildingPosition::parse).flatMap(java.util.Optional::stream)
                    .filter(p -> !nearest || p.dimension().equals(source.getLevel().dimension().identifier()))
                    .sorted(Comparator.comparingDouble(p -> p.pos().distToCenterSqr(source.getPosition())))
                    .limit(nearest ? 1 : 20).toList();
            source.sendSuccess(() -> Component.literal("Registered Main Bases: " + data.mainBaseKeys().size() + "; cleaned " + cleaned
                    + " stale building records. Activation stage: " + StructureConfig.ACTIVATION_STAGE.get() + ". Showing " + positions.size() + " (maximum 20)."), false);
            for (var pos : positions) {
                boolean loaded = pos.dimension().equals(source.getLevel().dimension().identifier()) && source.getLevel().hasChunkAt(pos.pos());
                boolean active = data.isMainBase(pos) && stage >= StructureConfig.ACTIVATION_STAGE.get();
                source.sendSuccess(() -> Component.literal(pos.key() + (active ? " [stage eligible]" : " [dormant]")
                        + (loaded ? " [loaded]" : " [not inspected/unloaded]")), false);
            }
            return positions.size();
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
}
