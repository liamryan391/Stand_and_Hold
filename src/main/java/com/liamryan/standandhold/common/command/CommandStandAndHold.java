package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class CommandStandAndHold extends CommandBase {
    private static final String[] SUBCOMMANDS = new String[] {
            "status",
            "addpoints",
            "setstage"
    };

    @Override
    public String getName() {
        return "standandhold";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.standandhold.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sendStatus(sender);
            return;
        }

        if ("addpoints".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeAddPoints(sender, args);
            return;
        }

        if ("setstage".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeSetStage(sender, args);
            return;
        }

        throw new CommandException("commands.standandhold.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }

        if (args.length == 2 && "setstage".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, getStageCompletions());
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    private void sendStatus(ICommandSender sender) {
        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        HumanStage stage = data.getStage();
        String nextThreshold = getNextThresholdLabel(stage);
        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.status",
                data.getHumanPoints(),
                stage.getId(),
                stage.getDisplayName(),
                nextThreshold
        );
        message.getStyle().setColor(TextFormatting.GREEN);
        sender.sendMessage(message);
    }

    private void executeAddPoints(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.addpoints.usage");
        }

        int amount = parseInt(args[1], 1);
        int total = HumanPointManager.addPoints(sender.getEntityWorld(), amount, "command");
        HumanStage stage = HumanPointManager.getData(sender.getEntityWorld()).getStage();

        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.addpoints.success",
                amount,
                total,
                stage.getId(),
                stage.getDisplayName()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        sender.sendMessage(message);
    }

    private void executeSetStage(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.setstage.usage");
        }

        HumanStage stage = HumanStage.fromCommandArgument(args[1]);
        if (stage == null) {
            throw new CommandException("commands.standandhold.setstage.invalid", args[1]);
        }

        HumanPointManager.setStage(sender.getEntityWorld(), stage);
        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.setstage.success",
                stage.getId(),
                stage.getDisplayName()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        sender.sendMessage(message);
    }

    private void requireAdmin(ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, getName())) {
            throw new CommandException("commands.generic.permission");
        }
    }

    private String getNextThresholdLabel(HumanStage currentStage) {
        int nextStageId = currentStage.getId() + 1;
        if (nextStageId >= HumanStage.values().length) {
            return "Max stage";
        }
        return Integer.toString(StandAndHoldConfig.getStageThreshold(nextStageId));
    }

    private String[] getStageCompletions() {
        List<String> completions = new ArrayList<String>();
        for (HumanStage stage : HumanStage.values()) {
            completions.add(Integer.toString(stage.getId()));
            completions.add(stage.getCommandName());
        }
        return completions.toArray(new String[completions.size()]);
    }
}
