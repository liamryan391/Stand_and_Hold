package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.threat.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

final class CommandResponse {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(value -> Component.literal(String.valueOf(value)));
    private CommandResponse() { }
    static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("threat").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(c -> inspect(c.getSource(), false)))
                .then(Commands.literal("list").executes(c -> inspect(c.getSource(), true)))
                .then(Commands.literal("reset").executes(c -> reset(c.getSource(), null))
                        .then(Commands.argument("region", StringArgumentType.string()).executes(c -> reset(c.getSource(), StringArgumentType.getString(c, "region"))))));
        root.then(Commands.literal("event").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(c -> event(c.getSource(), "status")))
                .then(Commands.literal("raid").executes(c -> event(c.getSource(), "raid")))
                .then(Commands.literal("reinforcement").executes(c -> event(c.getSource(), "reinforcement"))));
    }
    private static int inspect(CommandSourceStack source, boolean list) throws CommandSyntaxException {
        try {
            var level = source.getLevel(); var data = ThreatWorldData.get(source.getServer());
            var keys = list ? data.regionKeys().stream().map(ThreatRegion::parse).flatMap(java.util.Optional::stream).toList()
                    : java.util.List.of(ThreatResponse.region(level, BlockPos.containing(source.getPosition())));
            var values = keys.stream().map(key -> java.util.Map.entry(key, ThreatResponse.inspect(level, key)))
                    .sorted(Comparator.<java.util.Map.Entry<ThreatRegion, ThreatRecord>>comparingLong(e -> e.getValue().score()).reversed()).limit(20).toList();
            source.sendSuccess(() -> Component.literal("Threat regions: " + data.regionKeys().size() + "; showing " + values.size() + " (max 20), lazy score decay; counters are historical."), false);
            for (var entry : values) {
                var r = entry.getValue(); source.sendSuccess(() -> Component.literal(entry.getKey().key() + " | score " + r.score() + " | mapped deaths " + r.deaths()
                        + ", human/player kills " + r.kills() + ", human losses " + r.losses() + ", attack windows " + r.attacks()), false);
            }
            return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
    private static int reset(CommandSourceStack source, String key) throws CommandSyntaxException {
        try {
            var region = key == null ? ThreatResponse.region(source.getLevel(), BlockPos.containing(source.getPosition()))
                    : ThreatRegion.parse(key).orElseThrow(() -> new IllegalArgumentException("Expected dimension@regionX,regionZ (quote the argument)."));
            boolean removed = ThreatWorldData.get(source.getServer()).resetRegion(region);
            source.sendSuccess(() -> Component.literal((removed ? "Reset " : "No saved record for ") + region.key() + ". Event cooldowns, raiders and defender quotas are unchanged."), true); return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
    private static int event(CommandSourceStack source, String action) throws CommandSyntaxException {
        try {
            var level = source.getLevel(); var post = ThreatResponse.nearest(level, BlockPos.containing(source.getPosition()));
            String message;
            if (action.equals("raid")) { message = ThreatResponse.raid(level, post); }
            else if (action.equals("reinforcement")) { message = ThreatResponse.reinforce(level, post); }
            else {
                var state = ThreatResponse.state(level, post); long now = level.getGameTime();
                message = "Nearest post " + post.getBlockPos().toShortString() + ": natural grace " + Math.max(0, state.naturalAfter() - now) + " ticks; raid cooldown " + Math.max(0, state.nextRaid() - now)
                        + " ticks, reinforcement cooldown " + Math.max(0, state.nextReinforcement() - now) + " ticks; reserved raiders " + state.raiders().size()
                        + "; raids " + state.raids() + ", reinforcements " + state.reinforcements() + "; patrol region " + ThreatResponse.patrolRegion(level, post.getBlockPos()).key() + ".";
            }
            source.sendSuccess(() -> Component.literal(message), !action.equals("status")); return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
}
