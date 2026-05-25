package com.liamryan.standandhold.common.command;

import com.liamryan.standandhold.common.infrastructure.FieldCommandPostLevel;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeManager;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeRequirement;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
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

public final class CommandStandAndHold extends CommandBase {
    private static final String[] SUBCOMMANDS = new String[] {
            "status",
            "addpoints",
            "setstage",
            "research",
            "commandpost",
            "structure"
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

        if ("structure".equalsIgnoreCase(args[0])) {
            requireAdmin(sender);
            executeStructure(sender, args);
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

        if (args.length == 3 && "research".equalsIgnoreCase(args[0]) && "complete".equalsIgnoreCase(args[1])) {
            return getListOfStringsMatchingLastWord(args, getResearchCompletions());
        }

        if (args.length > 2 && args.length <= 5
                && "commandpost".equalsIgnoreCase(args[0])
                && ("status".equalsIgnoreCase(args[1]) || "upgrade".equalsIgnoreCase(args[1]))) {
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
                stage.getDisplayName()
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
            default:
                throw new CommandException("commands.standandhold.commandpost.upgrade.config", 0, "unknown");
        }

        message.getStyle().setColor(color);
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
        String reward = entry.getCompletionPointReward() > 0 ? " +" + entry.getCompletionPointReward() + " points" : "";
        return "- " + entry.getId() + " [" + state + "] "
                + entry.getCategory().getDisplayName() + " - "
                + entry.getDisplayName() + reward + sampleCost + requirements;
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
