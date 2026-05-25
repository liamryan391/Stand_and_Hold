package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.dynamic.DynamicEventManager;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeManager;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeRequirement;
import com.liamryan.standandhold.common.infrastructure.MainBaseManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.threat.ThreatRecord;
import com.liamryan.standandhold.common.threat.ThreatResponseManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.common.worldgen.ArmyCheckpointWorldGenerator;
import com.liamryan.standandhold.common.worldgen.MainBaseWorldGenerator;
import com.liamryan.standandhold.config.StandAndHoldConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CommandStandAndHold extends CommandBase {
    private static final String[] SUBCOMMANDS = new String[] {
            "status",
            "addpoints",
            "setstage",
            "research",
            "commandpost",
            "mainbase",
            "threat",
            "supplies",
            "structure",
            "event"
    };
    private static final String[] RESEARCH_SUBCOMMANDS = new String[] {
            "list",
            "complete",
            "status"
    };
    private static final String[] COMMAND_POST_SUBCOMMANDS = new String[] {
            "list",
            "nearest",
            "status",
            "upgrade"
    };
    private static final String[] STRUCTURE_SUBCOMMANDS = new String[] {
            "checkpoint",
            "mainbase"
    };
    private static final String[] MAIN_BASE_SUBCOMMANDS = new String[] {
            "list",
            "status",
            "activate"
    };
    private static final String[] THREAT_SUBCOMMANDS = new String[] {
            "status",
            "list",
            "reinforce",
            "reset"
    };
    private static final String[] SUPPLY_SUBCOMMANDS = new String[] {
            "status",
            "add"
    };
    private static final String[] EVENT_SUBCOMMANDS = new String[] {
            "outpostattack",
            "reinforcement"
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

        if ("research".equalsIgnoreCase(args[0])) {
            executeResearch(sender, args);
            return;
        }

        if ("commandpost".equalsIgnoreCase(args[0])) {
            executeCommandPost(sender, args);
            return;
        }

        if ("mainbase".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeMainBase(sender, args);
            return;
        }

        if ("threat".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeThreat(sender, args);
            return;
        }

        if ("supplies".equalsIgnoreCase(args[0])) {
            executeSupplies(sender, args);
            return;
        }

        if ("structure".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeStructure(sender, args);
            return;
        }

        if ("event".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeDynamicEvent(sender, args);
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

        if (args.length == 2 && "research".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, RESEARCH_SUBCOMMANDS);
        }

        if (args.length == 2 && "commandpost".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, COMMAND_POST_SUBCOMMANDS);
        }

        if (args.length == 2 && "structure".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, STRUCTURE_SUBCOMMANDS);
        }

        if (args.length == 2 && "mainbase".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, MAIN_BASE_SUBCOMMANDS);
        }

        if (args.length == 2 && "threat".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, THREAT_SUBCOMMANDS);
        }

        if (args.length == 2 && "supplies".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, SUPPLY_SUBCOMMANDS);
        }

        if (args.length == 2 && "event".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, EVENT_SUBCOMMANDS);
        }

        if (args.length == 3 && "research".equalsIgnoreCase(args[0]) && "complete".equalsIgnoreCase(args[1])) {
            return getListOfStringsMatchingLastWord(args, getResearchCompletions());
        }

        if (args.length > 2 && args.length <= 5
                && "commandpost".equalsIgnoreCase(args[0])
                && ("status".equalsIgnoreCase(args[1]) || "upgrade".equalsIgnoreCase(args[1]))) {
            return getTabCompletionCoordinate(args, 2, targetPos);
        }

        if (args.length > 2 && args.length <= 5
                && "mainbase".equalsIgnoreCase(args[0])
                && ("status".equalsIgnoreCase(args[1]) || "activate".equalsIgnoreCase(args[1]))) {
            return getTabCompletionCoordinate(args, 2, targetPos);
        }

        if (args.length > 2 && args.length <= 5 && "event".equalsIgnoreCase(args[0])) {
            return getTabCompletionCoordinate(args, 2, targetPos);
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
                nextThreshold,
                data.getSupplyPoints()
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

    private void executeSupplies(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("commands.standandhold.supplies.usage");
        }

        if ("status".equalsIgnoreCase(args[1])) {
            if (args.length != 2) {
                throw new CommandException("commands.standandhold.supplies.usage");
            }

            TextComponentTranslation message = new TextComponentTranslation(
                    "commands.standandhold.supplies.status",
                    SupplyManager.getSupplyPoints(sender.getEntityWorld())
            );
            message.getStyle().setColor(TextFormatting.AQUA);
            sender.sendMessage(message);
            return;
        }

        if ("add".equalsIgnoreCase(args[1])) {
            requireAdmin(sender);
            if (args.length != 3) {
                throw new CommandException("commands.standandhold.supplies.add.usage");
            }

            int amount = parseInt(args[2], 1);
            int total = SupplyManager.addSupplies(sender.getEntityWorld(), amount, "command");
            TextComponentTranslation message = new TextComponentTranslation(
                    "commands.standandhold.supplies.add.success",
                    amount,
                    total
            );
            message.getStyle().setColor(TextFormatting.YELLOW);
            sender.sendMessage(message);
            return;
        }

        throw new CommandException("commands.standandhold.supplies.usage");
    }

    private void executeResearch(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("commands.standandhold.research.usage");
        }

        if ("list".equalsIgnoreCase(args[1])) {
            executeResearchList(sender, args);
            return;
        }

        if ("status".equalsIgnoreCase(args[1])) {
            executeResearchStatus(sender, args);
            return;
        }

        if ("complete".equalsIgnoreCase(args[1])) {
            requireAdmin(sender);
            executeResearchComplete(sender, args);
            return;
        }

        throw new CommandException("commands.standandhold.research.usage");
    }

    private void executeResearchList(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.research.usage");
        }

        List<ResearchEntry> entries = ResearchManager.getResearchEntries();
        TextComponentTranslation header = new TextComponentTranslation("commands.standandhold.research.list.header", entries.size());
        header.getStyle().setColor(TextFormatting.AQUA);
        sender.sendMessage(header);

        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        for (ResearchEntry entry : entries) {
            TextFormatting color = data.isResearchCompleted(entry.getId()) ? TextFormatting.GREEN : TextFormatting.GRAY;
            String state = data.isResearchCompleted(entry.getId()) ? "complete" : "open";
            TextComponentString line = new TextComponentString(formatResearchEntry(entry, state));
            line.getStyle().setColor(color);
            sender.sendMessage(line);
        }
    }

    private void executeResearchStatus(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.research.usage");
        }

        int completedCount = ResearchManager.getCompletedResearchCount(sender.getEntityWorld());
        int totalCount = ResearchManager.getResearchEntries().size();
        TextComponentTranslation header = new TextComponentTranslation("commands.standandhold.research.status", completedCount, totalCount);
        header.getStyle().setColor(TextFormatting.AQUA);
        sender.sendMessage(header);

        List<ResearchEntry> completedEntries = ResearchManager.getCompletedResearchEntries(sender.getEntityWorld());
        if (completedEntries.isEmpty()) {
            TextComponentTranslation none = new TextComponentTranslation("commands.standandhold.research.status.none");
            none.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(none);
            return;
        }

        for (ResearchEntry entry : completedEntries) {
            TextComponentString line = new TextComponentString("- " + entry.getId() + " (" + entry.getDisplayName() + ")");
            line.getStyle().setColor(TextFormatting.GREEN);
            sender.sendMessage(line);
        }
    }

    private void executeResearchComplete(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) {
            throw new CommandException("commands.standandhold.research.complete.usage");
        }

        ResearchManager.CompletionResult result = ResearchManager.completeResearch(sender.getEntityWorld(), args[2], getPlayerSender(sender));
        if (result.getStatus() == ResearchManager.CompletionStatus.UNKNOWN_RESEARCH) {
            throw new CommandException("commands.standandhold.research.complete.unknown", args[2]);
        }

        ResearchEntry entry = result.getEntry();
        if (result.getStatus() == ResearchManager.CompletionStatus.ALREADY_COMPLETE) {
            throw new CommandException("commands.standandhold.research.complete.already", entry.getId());
        }

        if (result.getStatus() == ResearchManager.CompletionStatus.MISSING_REQUIREMENTS) {
            throw new CommandException("commands.standandhold.research.complete.missing", entry.getId(), joinStrings(result.getMissingRequirements()));
        }

        if (result.getStatus() == ResearchManager.CompletionStatus.MISSING_SAMPLES) {
            throw new CommandException("commands.standandhold.research.complete.samples", entry.getId(), result.getRequiredSamples(), result.getAvailableSamples());
        }

        if (result.getStatus() == ResearchManager.CompletionStatus.MISSING_SUPPLIES) {
            throw new CommandException("commands.standandhold.research.complete.supplies", entry.getId(), result.getRequiredSupplies(), result.getAvailableSupplies());
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.research.complete.success",
                entry.getId(),
                entry.getDisplayName(),
                entry.getCompletionPointReward()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        sender.sendMessage(message);
    }

    private void executeCommandPost(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("commands.standandhold.commandpost.usage");
        }

        if ("list".equalsIgnoreCase(args[1])) {
            requireAdmin(sender);
            executeCommandPostList(sender, args);
            return;
        }

        if ("nearest".equalsIgnoreCase(args[1])) {
            requireAdmin(sender);
            executeCommandPostNearest(sender, args);
            return;
        }

        if ("status".equalsIgnoreCase(args[1])) {
            executeCommandPostStatus(sender, args);
            return;
        }

        if ("upgrade".equalsIgnoreCase(args[1])) {
            requireAdmin(sender);
            executeCommandPostUpgrade(sender, args);
            return;
        }

        throw new CommandException("commands.standandhold.commandpost.usage");
    }

    private void executeCommandPostList(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.commandpost.usage");
        }

        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        int total = data.getFieldCommandPostPositions().size();
        TextComponentTranslation header = new TextComponentTranslation("commands.standandhold.commandpost.list.header", total);
        header.getStyle().setColor(TextFormatting.AQUA);
        sender.sendMessage(header);

        if (total == 0) {
            TextComponentTranslation none = new TextComponentTranslation("commands.standandhold.commandpost.list.none");
            none.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(none);
            return;
        }

        int shown = 0;
        int limit = 10;
        for (String positionKey : data.getFieldCommandPostPositions()) {
            PositionRecord record = PositionRecord.parse(positionKey);
            if (record == null) {
                continue;
            }

            TextComponentTranslation line = new TextComponentTranslation(
                    "commands.standandhold.commandpost.list.entry",
                    record.dimension,
                    record.pos.getX(),
                    record.pos.getY(),
                    record.pos.getZ()
            );
            line.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(line);
            shown++;
            if (shown >= limit) {
                break;
            }
        }

        if (total > shown) {
            TextComponentTranslation more = new TextComponentTranslation("commands.standandhold.commandpost.list.more", total - shown);
            more.getStyle().setColor(TextFormatting.DARK_GRAY);
            sender.sendMessage(more);
        }
    }

    private void executeCommandPostNearest(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.commandpost.usage");
        }

        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        int currentDimension = sender.getEntityWorld().provider.getDimension();
        BlockPos senderPos = sender.getPosition();
        PositionRecord nearest = null;
        long nearestDistance = Long.MAX_VALUE;

        for (String positionKey : data.getFieldCommandPostPositions()) {
            PositionRecord record = PositionRecord.parse(positionKey);
            if (record == null || record.dimension != currentDimension) {
                continue;
            }

            long distance = distanceSq(senderPos, record.pos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = record;
            }
        }

        if (nearest == null) {
            TextComponentTranslation none = new TextComponentTranslation("commands.standandhold.commandpost.nearest.none", currentDimension);
            none.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(none);
            return;
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.commandpost.nearest",
                nearest.pos.getX(),
                nearest.pos.getY(),
                nearest.pos.getZ(),
                Math.round(Math.sqrt(nearestDistance))
        );
        message.getStyle().setColor(TextFormatting.GREEN);
        sender.sendMessage(message);
    }

    private void executeCommandPostStatus(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 5) {
            throw new CommandException("commands.standandhold.commandpost.usage");
        }

        BlockPos pos = parseBlockPos(sender, args, 2, false);
        TileEntityFieldCommandPost commandPost = getCommandPostAt(sender, pos);
        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        HumanStage stage = data.getStage();
        FieldCommandPostLevel level = commandPost.getUpgradeLevelInfo();
        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.commandpost.status",
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                level.getLevel(),
                level.getDisplayName(),
                data.getHumanPoints(),
                stage.getId(),
                stage.getDisplayName(),
                data.getSupplyPoints(),
                commandPost.getStoredSupplies(),
                commandPost.getMaxStoredSupplies()
        );
        message.getStyle().setColor(TextFormatting.GREEN);
        sender.sendMessage(message);
    }

    private void executeCommandPostUpgrade(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 5) {
            throw new CommandException("commands.standandhold.commandpost.usage");
        }

        BlockPos pos = parseBlockPos(sender, args, 2, false);
        TileEntityFieldCommandPost commandPost = getCommandPostAt(sender, pos);
        FieldCommandPostUpgradeManager.UpgradeResult result = FieldCommandPostUpgradeManager.tryUpgrade(sender.getEntityWorld(), commandPost, getPlayerSender(sender));
        sendCommandPostUpgradeResult(sender, result);
    }

    private void executeMainBase(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("commands.standandhold.mainbase.usage");
        }

        if ("list".equalsIgnoreCase(args[1])) {
            executeMainBaseList(sender, args);
            return;
        }

        if ("status".equalsIgnoreCase(args[1])) {
            executeMainBaseStatus(sender, args);
            return;
        }

        if ("activate".equalsIgnoreCase(args[1])) {
            executeMainBaseActivate(sender, args);
            return;
        }

        throw new CommandException("commands.standandhold.mainbase.usage");
    }

    private void executeMainBaseList(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.mainbase.usage");
        }

        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        int total = data.getMainBasePositions().size();
        int active = data.getActiveMainBasePositions().size();
        TextComponentTranslation header = new TextComponentTranslation("commands.standandhold.mainbase.list.header", total, active);
        header.getStyle().setColor(TextFormatting.AQUA);
        sender.sendMessage(header);

        if (total == 0) {
            TextComponentTranslation none = new TextComponentTranslation("commands.standandhold.mainbase.list.none");
            none.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(none);
            return;
        }

        int shown = 0;
        int limit = 10;
        for (String positionKey : data.getMainBasePositions()) {
            PositionRecord record = PositionRecord.parse(positionKey);
            if (record == null) {
                continue;
            }

            TextComponentTranslation line = new TextComponentTranslation(
                    "commands.standandhold.mainbase.list.entry",
                    record.dimension,
                    record.pos.getX(),
                    record.pos.getY(),
                    record.pos.getZ(),
                    data.isMainBaseActive(record.dimension, record.pos) ? "active" : "dormant"
            );
            line.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(line);
            shown++;
            if (shown >= limit) {
                break;
            }
        }

        if (total > shown) {
            TextComponentTranslation more = new TextComponentTranslation("commands.standandhold.mainbase.list.more", total - shown);
            more.getStyle().setColor(TextFormatting.DARK_GRAY);
            sender.sendMessage(more);
        }
    }

    private void executeMainBaseStatus(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 5) {
            throw new CommandException("commands.standandhold.mainbase.usage");
        }

        BlockPos pos = parseBlockPos(sender, args, 2, false);
        HumanWorldData data = HumanPointManager.getData(sender.getEntityWorld());
        int dimension = sender.getEntityWorld().provider.getDimension();
        if (!data.isMainBaseRegistered(dimension, pos)) {
            throw new CommandException("commands.standandhold.mainbase.not_found", pos.getX(), pos.getY(), pos.getZ());
        }

        boolean active = data.isMainBaseActive(dimension, pos);
        boolean activationReady = MainBaseManager.canActivateMainBase(sender.getEntityWorld());
        boolean specialReady = MainBaseManager.isSpecialParasiteDivisionUnlocked(sender.getEntityWorld());
        int specialOperatives = sender.getEntityWorld().isBlockLoaded(pos) ? MainBaseManager.countAssignedSpecialOperatives(sender.getEntityWorld(), pos) : 0;
        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.mainbase.status",
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                active ? "active" : "dormant",
                activationReady ? "ready" : "locked",
                specialReady ? "unlocked" : "locked",
                specialOperatives
        );
        message.getStyle().setColor(TextFormatting.GREEN);
        sender.sendMessage(message);
    }

    private void executeMainBaseActivate(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 5) {
            throw new CommandException("commands.standandhold.mainbase.usage");
        }

        BlockPos pos = parseBlockPos(sender, args, 2, false);
        MainBaseManager.ActivationResult result = MainBaseManager.tryActivateMainBase(sender.getEntityWorld(), pos, true);
        switch (result.getStatus()) {
            case ACTIVATED:
                TextComponentTranslation message = new TextComponentTranslation(
                        "commands.standandhold.mainbase.activate.success",
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        result.getDefendersSpawned()
                );
                message.getStyle().setColor(TextFormatting.YELLOW);
                sender.sendMessage(message);
                return;
            case ALREADY_ACTIVE:
                throw new CommandException("commands.standandhold.mainbase.activate.already", pos.getX(), pos.getY(), pos.getZ());
            case LOCKED:
                throw new CommandException("commands.standandhold.mainbase.activate.locked", Math.max(5, StandAndHoldConfig.worldGeneration.mainBaseActivationStage));
            case NOT_REGISTERED:
                throw new CommandException("commands.standandhold.mainbase.not_found", pos.getX(), pos.getY(), pos.getZ());
            case NOT_LOADED:
                throw new CommandException("commands.standandhold.mainbase.activate.not_loaded", pos.getX(), pos.getY(), pos.getZ());
            case MISSING_COMMAND_POST:
                throw new CommandException("commands.standandhold.mainbase.activate.missing_command_post", pos.getX(), pos.getY(), pos.getZ());
            default:
                throw new CommandException("commands.standandhold.mainbase.activate.failed", pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private void executeThreat(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("commands.standandhold.threat.usage");
        }

        if ("status".equalsIgnoreCase(args[1])) {
            executeThreatStatus(sender, args);
            return;
        }

        if ("list".equalsIgnoreCase(args[1])) {
            executeThreatList(sender, args);
            return;
        }

        if ("reinforce".equalsIgnoreCase(args[1])) {
            executeThreatReinforce(sender, args);
            return;
        }

        if ("reset".equalsIgnoreCase(args[1])) {
            executeThreatReset(sender, args);
            return;
        }

        throw new CommandException("commands.standandhold.threat.usage");
    }

    private void executeThreatStatus(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.threat.usage");
        }

        ThreatRecord record = ThreatResponseManager.getThreatRecordAt(sender.getEntityWorld(), sender.getPosition(), false);
        if (record == null) {
            TextComponentTranslation none = new TextComponentTranslation("commands.standandhold.threat.status.none");
            none.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(none);
            return;
        }

        sendThreatStatus(sender, record);
    }

    private void executeThreatList(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.threat.usage");
        }

        List<ThreatRecord> records = ThreatResponseManager.getThreatRecordsSorted(sender.getEntityWorld());
        TextComponentTranslation header = new TextComponentTranslation("commands.standandhold.threat.list.header", records.size());
        header.getStyle().setColor(TextFormatting.AQUA);
        sender.sendMessage(header);

        if (records.isEmpty()) {
            TextComponentTranslation none = new TextComponentTranslation("commands.standandhold.threat.list.none");
            none.getStyle().setColor(TextFormatting.GRAY);
            sender.sendMessage(none);
            return;
        }

        int shown = 0;
        int limit = 10;
        for (ThreatRecord record : records) {
            TextComponentTranslation line = new TextComponentTranslation(
                    "commands.standandhold.threat.list.entry",
                    record.getDimension(),
                    record.getRegionX(),
                    record.getRegionZ(),
                    record.getThreatScore(),
                    record.getThreatLevel().getDisplayName(),
                    record.getParasiteKills(),
                    record.getHumanLosses(),
                    record.getOutpostAttacks(),
                    record.getReinforcementsSent()
            );
            line.getStyle().setColor(record.getThreatLevel().getId() >= 2 ? TextFormatting.RED : TextFormatting.GRAY);
            sender.sendMessage(line);
            shown++;
            if (shown >= limit) {
                break;
            }
        }

        if (records.size() > shown) {
            TextComponentTranslation more = new TextComponentTranslation("commands.standandhold.threat.list.more", records.size() - shown);
            more.getStyle().setColor(TextFormatting.DARK_GRAY);
            sender.sendMessage(more);
        }
    }

    private void executeThreatReinforce(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.threat.usage");
        }

        ThreatRecord record = ThreatResponseManager.getThreatRecordAt(sender.getEntityWorld(), sender.getPosition(), false);
        if (record == null) {
            throw new CommandException("commands.standandhold.threat.reinforce.no_record");
        }

        ThreatResponseManager.ReinforcementResult result = ThreatResponseManager.triggerReinforcement(sender.getEntityWorld(), record, true);
        if (result.getStatus() != ThreatResponseManager.ReinforcementStatus.DEPLOYED) {
            throw new CommandException("commands.standandhold.threat.reinforce.failed", result.getStatus().name().toLowerCase());
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.threat.reinforce.success",
                result.getUnitsSpawned(),
                record.getDimension(),
                record.getRegionX(),
                record.getRegionZ()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        sender.sendMessage(message);
    }

    private void executeThreatReset(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.threat.usage");
        }

        ThreatRecord record = ThreatResponseManager.getThreatRecordAt(sender.getEntityWorld(), sender.getPosition(), false);
        if (record == null) {
            throw new CommandException("commands.standandhold.threat.reset.no_record");
        }

        ThreatResponseManager.resetThreatRecord(sender.getEntityWorld(), record);
        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.threat.reset.success",
                record.getDimension(),
                record.getRegionX(),
                record.getRegionZ()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        sender.sendMessage(message);
    }

    private void executeDynamicEvent(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2 && args.length != 5) {
            throw new CommandException("commands.standandhold.event.usage");
        }

        BlockPos eventPos = args.length == 5 ? parseBlockPos(sender, args, 2, false) : sender.getPosition();
        DynamicEventManager.DynamicEventResult result;
        if ("outpostattack".equalsIgnoreCase(args[1])) {
            result = DynamicEventManager.triggerOutpostAttack(sender.getEntityWorld(), eventPos, true);
        } else if ("reinforcement".equalsIgnoreCase(args[1])) {
            result = DynamicEventManager.triggerHumanReinforcement(sender.getEntityWorld(), eventPos, true);
        } else {
            throw new CommandException("commands.standandhold.event.usage");
        }

        if (result.getStatus() != DynamicEventManager.DynamicEventStatus.STARTED) {
            throw new CommandException(
                    "commands.standandhold.event.failed",
                    result.getType().getDisplayName(),
                    eventPos.getX(),
                    eventPos.getY(),
                    eventPos.getZ(),
                    result.getStatus().name().toLowerCase(Locale.ROOT)
            );
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.event.success",
                result.getType().getDisplayName(),
                result.getPos().getX(),
                result.getPos().getY(),
                result.getPos().getZ(),
                result.getSpawnedCount(),
                result.getStageId()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        sender.sendMessage(message);
    }

    private void executeStructure(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.standandhold.structure.usage");
        }

        BlockPos senderPos = sender.getPosition();
        int chunkX = senderPos.getX() >> 4;
        int chunkZ = senderPos.getZ() >> 4;

        if ("checkpoint".equalsIgnoreCase(args[1])) {
            BlockPos origin = ArmyCheckpointWorldGenerator.forceGenerateAtChunk(sender.getEntityWorld(), chunkX, chunkZ);
            if (origin == null) {
                throw new CommandException("commands.standandhold.structure.generate.failed", "Small Army Checkpoint", "unsafe terrain");
            }

            TextComponentTranslation message = new TextComponentTranslation(
                    "commands.standandhold.structure.generate.checkpoint",
                    origin.getX(),
                    origin.getY(),
                    origin.getZ()
            );
            message.getStyle().setColor(TextFormatting.YELLOW);
            sender.sendMessage(message);
            return;
        }

        if ("mainbase".equalsIgnoreCase(args[1])) {
            MainBaseWorldGenerator.GenerationResult result = MainBaseWorldGenerator.forceGenerateAtChunk(sender.getEntityWorld(), chunkX, chunkZ);
            if (!result.isGenerated()) {
                throw new CommandException("commands.standandhold.structure.generate.failed", "Main Base", result.getReason());
            }

            BlockPos origin = result.getOrigin();
            TextComponentTranslation message = new TextComponentTranslation(
                    "commands.standandhold.structure.generate.mainbase",
                    origin.getX(),
                    origin.getY(),
                    origin.getZ(),
                    result.isActive() ? "active" : "dormant",
                    result.getDefendersSpawned()
            );
            message.getStyle().setColor(TextFormatting.YELLOW);
            sender.sendMessage(message);
            return;
        }

        throw new CommandException("commands.standandhold.structure.usage");
    }

    private TileEntityFieldCommandPost getCommandPostAt(ICommandSender sender, BlockPos pos) throws CommandException {
        TileEntity tileEntity = sender.getEntityWorld().getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityFieldCommandPost)) {
            throw new CommandException("commands.standandhold.commandpost.not_found", pos.getX(), pos.getY(), pos.getZ());
        }

        return (TileEntityFieldCommandPost) tileEntity;
    }

    private void sendCommandPostUpgradeResult(ICommandSender sender, FieldCommandPostUpgradeManager.UpgradeResult result) throws CommandException {
        FieldCommandPostUpgradeRequirement requirement = result.getRequirement();
        FieldCommandPostLevel level = result.getLevel();
        TextComponentTranslation message;
        TextFormatting color = TextFormatting.RED;

        switch (result.getStatus()) {
            case COMPLETED:
                color = TextFormatting.YELLOW;
                message = new TextComponentTranslation(
                        "commands.standandhold.commandpost.upgrade.success",
                        level.getLevel(),
                        level.getDisplayName(),
                        requirement.getCompletionPointReward()
                );
                break;
            case ALREADY_MAX_LEVEL:
                message = new TextComponentTranslation(
                        "commands.standandhold.commandpost.upgrade.max",
                        level.getLevel(),
                        level.getDisplayName()
                );
                break;
            case MISSING_CONFIGURATION:
                throw new CommandException("commands.standandhold.commandpost.upgrade.config", level.getLevel(), level.getDisplayName());
            case MISSING_POINTS:
                throw new CommandException(
                        "commands.standandhold.commandpost.upgrade.points",
                        level.getLevel(),
                        requirement.getRequiredHumanPoints(),
                        result.getCurrentHumanPoints()
                );
            case MISSING_RESEARCH:
                throw new CommandException(
                        "commands.standandhold.commandpost.upgrade.research",
                        level.getLevel(),
                        joinStrings(result.getMissingResearchIds())
                );
            case MISSING_SAMPLES:
                throw new CommandException(
                        "commands.standandhold.commandpost.upgrade.samples",
                        level.getLevel(),
                        requirement.getParasiteSampleCost(),
                        result.getAvailableSamples()
                );
            case MISSING_SUPPLIES:
                throw new CommandException(
                        "commands.standandhold.commandpost.upgrade.supplies",
                        level.getLevel(),
                        requirement.getSupplyCost(),
                        result.getAvailableSupplies()
                );
            default:
                throw new CommandException("commands.standandhold.commandpost.upgrade.config", 0, "unknown");
        }

        message.getStyle().setColor(color);
        sender.sendMessage(message);
    }

    private void sendThreatStatus(ICommandSender sender, ThreatRecord record) {
        TextComponentTranslation message = new TextComponentTranslation(
                "commands.standandhold.threat.status",
                record.getDimension(),
                record.getRegionX(),
                record.getRegionZ(),
                record.getThreatScore(),
                record.getThreatLevel().getDisplayName(),
                record.getParasiteKills(),
                record.getHumanLosses(),
                record.getOutpostAttacks(),
                record.getReinforcementsSent()
        );
        message.getStyle().setColor(record.getThreatLevel().getId() >= 2 ? TextFormatting.RED : TextFormatting.GREEN);
        sender.sendMessage(message);
    }

    private long distanceSq(BlockPos first, BlockPos second) {
        long dx = first.getX() - second.getX();
        long dy = first.getY() - second.getY();
        long dz = first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
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

    private String[] getResearchCompletions() {
        List<String> completions = ResearchManager.getResearchIds();
        return completions.toArray(new String[completions.size()]);
    }

    private String formatResearchEntry(ResearchEntry entry, String state) {
        String requirements = entry.hasRequirements() ? " requires " + joinStrings(entry.getRequiredResearchIds()) : "";
        String sampleCost = entry.getParasiteSampleCost() > 0 ? " samples " + entry.getParasiteSampleCost() : "";
        String supplyCost = entry.getSupplyCost() > 0 ? " supplies " + entry.getSupplyCost() : "";
        String reward = entry.getCompletionPointReward() > 0 ? " +" + entry.getCompletionPointReward() + " points" : "";
        return "- " + entry.getId() + " [" + state + "] "
                + entry.getCategory().getDisplayName() + " - "
                + entry.getDisplayName() + reward + sampleCost + supplyCost + requirements;
    }

    private String joinStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private EntityPlayer getPlayerSender(ICommandSender sender) {
        Entity entity = sender.getCommandSenderEntity();
        return entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
    }

    private static final class PositionRecord {
        private final int dimension;
        private final BlockPos pos;

        private PositionRecord(int dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        @Nullable
        private static PositionRecord parse(String positionKey) {
            if (positionKey == null || positionKey.trim().isEmpty()) {
                return null;
            }

            String[] parts = positionKey.split(":");
            if (parts.length != 4) {
                return null;
            }

            try {
                int dimension = Integer.parseInt(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                return new PositionRecord(dimension, new BlockPos(x, y, z));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
