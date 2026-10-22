package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.mission.MissionState;
import com.liamryan.standandhold.common.progression.ProgressionRules;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

/** Explicit read-only guidance; no ticking, mission refresh, grants or world scans. */
public final class CommandGuide {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(message -> Component.literal(String.valueOf(message)));
    private CommandGuide() { }
    public static void addChildren(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("help").executes(context -> {
            var source = context.getSource();
            for (String line : help(operator(source))) { source.sendSuccess(() -> Component.literal(line), false); }
            return 1;
        }));
        root.then(Commands.literal("guide").executes(context -> {
            var source = context.getSource();
            try {
                for (String line : lines(HumanWorldData.get(source.getServer()), FoundationResources.get(source.getServer()),
                        HumanProgressionConfig.thresholds(), operator(source))) { source.sendSuccess(() -> component(line), false); }
                return 1;
            } catch (IllegalArgumentException | IllegalStateException exception) { throw ERROR.create(exception.getMessage()); }
        }));
    }
    private static boolean operator(CommandSourceStack source) { return Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source); }
    public static Component component(String line) {
        var text = Component.literal(line);
        if (line.startsWith("Start: /standandhold mission start ")) {
            text.withStyle(style -> style.withColor(ChatFormatting.AQUA).withUnderlined(true)
                    .withClickEvent(new ClickEvent.SuggestCommand(line.substring("Start: ".length())))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Fill chat only; review the command and press Enter to start."))));
        }
        return text;
    }
    public static List<String> help(boolean operator) {
        var lines = new ArrayList<>(List.of("Stand and Hold technical alpha: shared world progression; no external mod required.",
                "/standandhold guide - next eligible mission and its objective",
                "/standandhold status | supplies status | research list | mission active",
                "Research Lab: select a target, fund local storage, then Start; selecting alone never starts work."));
        if (operator) { lines.add("QA/operator: mission start <id> | commandpost nearest | event status | compat status"); }
        else { lines.add("Mission starts and debug/event commands require an operator; gameplay progress does not."); }
        return List.copyOf(lines);
    }
    public static List<String> lines(HumanWorldData data, FoundationCatalog catalog, long[] thresholds, boolean operator) {
        data.requireSupportedVersion();
        var lines = new ArrayList<String>();
        lines.add("Points " + data.points() + " | Stage " + ProgressionRules.stageFor(data.points(), thresholds).level()
                + " | Global supplies " + data.supplies());
        var next = catalog.missions().values().stream().filter(mission -> !complete(data, mission.id()))
                .filter(mission -> mission.prerequisites().stream().allMatch(id -> complete(data, id))).findFirst();
        if (next.isEmpty()) {
            lines.add(catalog.missions().values().stream().allMatch(mission -> complete(data, mission.id()))
                    ? "All missions in the loaded catalog are complete." : "No eligible mission; inspect prerequisites with /standandhold mission list.");
            return List.copyOf(lines);
        }
        Mission mission = next.get();
        var state = data.mission(mission.id());
        lines.add(mission.title() + " | " + state.map(value -> value.progress() + "/" + mission.target()).orElse("not started"));
        if (state.isEmpty()) { lines.add(operator ? "Start: /standandhold mission start " + mission.id() : "Ask an operator to start shared mission " + mission.id() + "."); }
        lines.add(hint(mission, catalog, thresholds));
        lines.add("Reward once: +" + mission.reward().points() + " points, +" + mission.reward().supplies() + " supplies.");
        lines.add("Read-only. Refresh: /standandhold mission status | /standandhold help");
        return List.copyOf(lines);
    }
    private static boolean complete(HumanWorldData data, String id) { return data.mission(id).filter(MissionState::completed).isPresent(); }
    private static String hint(Mission mission, FoundationCatalog catalog, long[] thresholds) {
        return switch (mission.objective()) {
            case SAMPLE_RECOVERY -> "Carry " + mission.target() + " Tissue Samples at once. Player-kill threats (default: zombies); random drops, not cumulative pickups.";
            case COMMAND_POST -> "Find/place a Field Command Post. Operator checkpoint generation is QA setup, not Survival acquisition.";
            case SUPPLY_TOTAL -> "Keep " + mission.target() + " GLOBAL supplies before lab imports. Already spent? Export naturally generated post supplies.";
            case RESEARCH_COMPLETION -> {
                var entry = catalog.research(mission.subject());
                yield "Lab: " + entry.title() + " | Stage " + entry.requiredStage() + ", " + entry.supplyCost()
                        + " LOCAL supplies, " + entry.sampleCost() + " samples + prerequisites. Select, fund, Start; stay nearby.";
            }
            case HUMAN_STAGE -> "Reach Stage " + mission.target() + " (" + thresholds[mission.target()] + " points) via kills/loaded posts. Existing defenders keep their tier.";
            case OUTPOST_DEFENCE -> "Defeat " + mission.target() + " reserved RAID attackers with player/human credit, across waves. Ordinary kills do not count; cooldowns/caps apply.";
            case MAIN_BASE_DISCOVERY -> "Find/use a Main Base command post; discovery is not activation (default Stage 5). Back up before removals.";
        };
    }
}
