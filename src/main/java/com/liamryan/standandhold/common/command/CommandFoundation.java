package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

final class CommandFoundation {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(message -> Component.literal(String.valueOf(message)));

    private CommandFoundation() { }

    static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("supplies")
                .executes(context -> supplyStatus(context.getSource()))
                .then(Commands.literal("status").executes(context -> supplyStatus(context.getSource())))
                .then(Commands.literal("add").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(context -> respond(context.getSource(),
                                () -> supplyChange(context.getSource(), LongArgumentType.getLong(context, "amount"), false), true))))
                .then(Commands.literal("spend").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(context -> respond(context.getSource(),
                                () -> supplyChange(context.getSource(), LongArgumentType.getLong(context, "amount"), true), true)))));
        root.then(Commands.literal("research")
                .executes(context -> researchList(context.getSource(), false))
                .then(Commands.literal("list").executes(context -> researchList(context.getSource(), true)))
                .then(Commands.literal("status").executes(context -> researchList(context.getSource(), false)))
                .then(Commands.literal("complete").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", StringArgumentType.word()).executes(context -> researchComplete(context.getSource(), StringArgumentType.getString(context, "id"))))));
        root.then(Commands.literal("mission")
                .executes(context -> missionList(context.getSource(), false, true))
                .then(Commands.literal("list").executes(context -> missionList(context.getSource(), false, false)))
                .then(Commands.literal("status").executes(context -> missionList(context.getSource(), false, true)))
                .then(Commands.literal("active").executes(context -> missionList(context.getSource(), true, false)))
                .then(Commands.literal("start").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", StringArgumentType.word()).executes(context -> missionChange(context.getSource(), StringArgumentType.getString(context, "id"), 0, true))))
                .then(Commands.literal("complete").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", StringArgumentType.word()).executes(context -> missionChange(context.getSource(), StringArgumentType.getString(context, "id"), Long.MAX_VALUE, false))))
                .then(Commands.literal("progress").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(context -> missionChange(context.getSource(),
                                        StringArgumentType.getString(context, "id"), LongArgumentType.getLong(context, "amount"), false))))));
    }

    private static int supplyStatus(CommandSourceStack source) throws CommandSyntaxException {
        return respond(source, () -> List.of("Global supplies: " + data(source).supplies()), false);
    }

    private static int researchList(CommandSourceStack source, boolean details) throws CommandSyntaxException {
        return respond(source, () -> {
            HumanWorldData data = data(source);
            FoundationCatalog catalog = catalog(source);
            List<String> lines = new ArrayList<>();
            lines.add("Research (global world progress):");
            for (ResearchEntry entry : catalog.research().values()) {
                lines.add(entry.id() + " - " + entry.title() + ": " + ResearchManager.blockedReason(data, entry, HumanProgressionConfig.thresholds())
                        + (details ? " | cost " + entry.supplyCost() + " supplies, " + entry.sampleCost() + " samples | unlocks " + entry.unlocks() : ""));
            }
            return lines;
        }, false);
    }

    private static int researchComplete(CommandSourceStack source, String id) throws CommandSyntaxException {
        return respond(source, () -> {
            FoundationCatalog catalog = catalog(source);
            ResearchEntry entry = catalog.research(id);
            boolean changed = ResearchManager.complete(data(source), catalog, HumanProgressionConfig.thresholds(), entry.id());
            List<String> lines = new ArrayList<>();
            lines.add(changed ? "Research complete: " + entry.title() + ". Spent " + entry.supplyCost() + " supplies. Reward: +"
                    + entry.reward().points() + " human points, +" + entry.reward().supplies() + " supplies."
                    : "Research already complete: " + entry.title() + "; no costs or rewards applied.");
            lines.addAll(MissionManager.refresh(data(source), catalog, HumanProgressionConfig.thresholds()));
            return lines;
        }, true);
    }

    private static List<String> supplyChange(CommandSourceStack source, long amount, boolean spend) {
        FoundationCatalog catalog = catalog(source);
        HumanWorldData data = data(source);
        long balance = spend ? SupplyManager.spend(data, amount) : SupplyManager.add(data, amount);
        List<String> lines = new ArrayList<>();
        lines.add("Global supplies: " + balance);
        lines.addAll(MissionManager.refresh(data, catalog, HumanProgressionConfig.thresholds()));
        return lines;
    }

    private static int missionList(CommandSourceStack source, boolean activeOnly, boolean refresh) throws CommandSyntaxException {
        return respond(source, () -> {
            HumanWorldData data = data(source);
            FoundationCatalog catalog = catalog(source);
            List<String> lines = new ArrayList<>();
            if (refresh) { lines.addAll(MissionManager.refresh(data, catalog, HumanProgressionConfig.thresholds())); }
            lines.add(activeOnly ? "Active missions (global):" : "Missions (global; start each mission explicitly):");
            int count = 0;
            for (Mission mission : catalog.missions().values()) {
                try {
                    var state = data.mission(mission.id());
                    if (activeOnly && (state.isEmpty() || state.get().completed())) { continue; }
                    String status = state.map(value -> value.completed() ? "completed" : value.progress() + "/" + mission.target()).orElse("not started");
                    lines.add(mission.id() + " - " + mission.title() + ": " + status + " | " + mission.objective()
                            + (activeOnly ? "" : " | requires " + mission.prerequisites()));
                    count++;
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    lines.add("Mission unavailable: " + mission.id() + " - " + exception.getMessage());
                }
            }
            if (count == 0) { lines.add("None."); }
            return lines;
        }, false);
    }

    private static int missionChange(CommandSourceStack source, String id, long amount, boolean start) throws CommandSyntaxException {
        return respond(source, () -> {
            FoundationCatalog catalog = catalog(source);
            HumanWorldData data = data(source);
            MissionManager.Update update = start ? MissionManager.start(data, catalog, HumanProgressionConfig.thresholds(), id)
                    : MissionManager.advance(data, catalog, HumanProgressionConfig.thresholds(), id, amount);
            String message = start && !update.state().completed()
                    ? "Mission active: " + update.mission().title() + " " + update.state().progress() + "/" + update.mission().target()
                    : update.message();
            List<String> lines = new ArrayList<>();
            lines.add(message);
            if (start && source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                lines.addAll(MissionManager.observeSamples(data, catalog, HumanProgressionConfig.thresholds(),
                        com.liamryan.standandhold.common.sample.SampleInventory.count(player.getInventory())));
            }
            return lines;
        }, true);
    }

    private static HumanWorldData data(CommandSourceStack source) { return HumanWorldData.get(source.getServer()); }
    private static FoundationCatalog catalog(CommandSourceStack source) {
        return com.liamryan.standandhold.common.foundation.FoundationResources.get(source.getServer());
    }

    private static int respond(CommandSourceStack source, Supplier<List<String>> action, boolean broadcast) throws CommandSyntaxException {
        try {
            List<String> lines = action.get();
            for (String line : lines) { source.sendSuccess(() -> Component.literal(line), broadcast); }
            return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw ERROR.create(exception.getMessage());
        }
    }
}
