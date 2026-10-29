package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.common.building.BuildingPositions;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.structure.StructureKind;
import com.liamryan.standandhold.common.structure.StructurePlacement;
import com.liamryan.standandhold.common.structure.OutpostPreview;
import com.liamryan.standandhold.common.structure.OutpostExpansion;
import com.liamryan.standandhold.common.structure.OutpostGrowth;
import com.liamryan.standandhold.common.support.SupportCallIn;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;

final class CommandStructures {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(value -> Component.literal(String.valueOf(value)));
    private CommandStructures() { }
    static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("outpost").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> growthHelp(context.getSource()))
                .then(Commands.literal("help").executes(context -> growthHelp(context.getSource())))
                .then(Commands.literal("preview").executes(context -> preview(context.getSource())))
                .then(Commands.literal("grow").executes(context -> growth(context.getSource(), null)))
                .then(Commands.literal("mode")
                        .then(Commands.literal("manual").executes(context -> growth(context.getSource(), OutpostGrowth.Mode.MANUAL)))
                        .then(Commands.literal("automatic").executes(context -> growth(context.getSource(), OutpostGrowth.Mode.AUTOMATIC))))
                .then(Commands.literal("prepare").executes(context -> expansion(context.getSource(), "prepare", null)))
                .then(Commands.literal("expand").then(Commands.argument("token", UuidArgument.uuid()).executes(context -> expansion(context.getSource(), "expand", UuidArgument.getUuid(context, "token")))))
                .then(Commands.literal("status").executes(context -> expansion(context.getSource(), "status", null)))
                .then(Commands.literal("recover").executes(context -> expansion(context.getSource(), "recover", null))));
        root.then(Commands.literal("structure").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("checkpoint").executes(context -> place(context.getSource(), StructureKind.CHECKPOINT)))
                .then(Commands.literal("mainbase").executes(context -> place(context.getSource(), StructureKind.MAIN_BASE))));
        root.then(Commands.literal("mainbase").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list").executes(context -> inspect(context.getSource(), false)))
                .then(Commands.literal("nearest").executes(context -> inspect(context.getSource(), true)))
                .then(Commands.literal("status").executes(context -> inspect(context.getSource(), true))));
    }
    private static int growthHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Face a loaded checkpoint post within 6 blocks. grow | mode manual | mode automatic; same rules as the Growth menu."), false);
        source.sendSuccess(() -> Component.literal("Advanced tools retained: preview | prepare | expand <token> | status | recover. Server expansionEnabled must allow placement; no repair/refill bypass."), false);
        return 1;
    }
    private static int growth(CommandSourceStack source, OutpostGrowth.Mode mode) throws CommandSyntaxException {
        try {
            var player = source.getPlayerOrException(); var post = SupportCallIn.lookedAt(player);
            if (mode == null) { OutpostGrowth.expand(player, post); }
            else { OutpostGrowth.setMode(player, post, mode); }
            source.sendSuccess(() -> Component.literal(mode == null ? "Outpost expanded once; buildings/storage/caps unchanged."
                    : "Outpost growth: " + mode + "; no resource spending or level change."), false);
            return 1;
        } catch (IllegalArgumentException | IllegalStateException failure) { throw ERROR.create(failure.getMessage()); }
    }
    private static int expansion(CommandSourceStack source, String action, java.util.UUID token) throws CommandSyntaxException {
        try {
            var player = source.getPlayerOrException(); var post = SupportCallIn.lookedAt(player);
            if (action.equals("prepare")) {
                var issued = OutpostExpansion.prepare(player, post);
                for (String line : new String[]{"Prepared: 137 cells / 88 ground; no writes yet.",
                        "Only this site/player; buildings/caps unchanged.",
                        "Player terrain may match. No claim-mod approval.",
                        "Back up first. Token expires in 10 unpaused seconds."}) { source.sendSuccess(() -> Component.literal(line), false); }
                String command = "/standandhold outpost expand " + issued;
                source.sendSuccess(() -> Component.literal("Confirm: " + command).withStyle(style -> style.withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.SuggestCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Fill chat only. Review ground-replacement consent, then press Enter to apply.")))), false);
                return 1;
            }
            String message;
            if (action.equals("expand")) {
                var result = OutpostExpansion.confirm(player, post, token);
                if (!result.success()) { throw new IllegalStateException(result.message()); } message = result.message();
            } else { message = action.equals("recover") ? OutpostExpansion.recover(player, post) : "Growth " + post.growthSettings().mode()
                    + " / " + OutpostGrowth.label(OutpostGrowth.status(player.level(), post)) + ". " + OutpostExpansion.status(player, post); }
            source.sendSuccess(() -> Component.literal(message), false); return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
    private static int preview(CommandSourceStack source) throws CommandSyntaxException {
        try {
            var player = source.getPlayerOrException();
            var post = SupportCallIn.lookedAt(player);
            var plan = OutpostPreview.inspect(player, post);
            var center = plan.site().center();
            String oldBounds = bounds(center, 4), newBounds = bounds(center, 6);
            for (String line : new String[]{"Checkpoint v1 / L" + post.state().level() + " / READ ONLY",
                    "Site: " + plan.site().id(), "Old " + oldBounds, "New " + newBounds,
                    "137 planned: 88 floor, 45 wall, 4 lights; buildings/caps unchanged.",
                    "Would replace 88 ground cells; player-placed terrain may match.",
                    "No writes/cost/rewards/token; no claim-mod approval."}) {
                source.sendSuccess(() -> Component.literal(line), false);
            }
            return 1;
        } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
    }
    private static String bounds(BlockPos center, int radius) {
        return "x:" + (center.getX() - radius) + ".." + (center.getX() + radius)
                + " z:" + (center.getZ() - radius) + ".." + (center.getZ() + radius) + " floor:" + center.getY();
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
