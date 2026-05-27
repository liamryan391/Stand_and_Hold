package com.liamryan.standandhold.common.network;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.research.ResearchEntry;
import com.liamryan.standandhold.common.research.ResearchManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketSyncTileData implements IMessage {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_FIELD_COMMAND_POST = 1;
    public static final int TYPE_RESEARCH_LAB = 2;

    private BlockPos pos = BlockPos.ORIGIN;
    private int typeId = TYPE_NONE;
    private int upgradeLevel;
    private int storedSupplies;
    private int maxStoredSupplies;
    private int researchProgress;
    private int researchProgressRequired;
    private int storedSamples;
    private int maxStoredSamples;
    private int targetSampleCost;
    private int targetSupplyCost;
    private String targetResearchId = "";
    private String targetResearchLabel = "";

    public PacketSyncTileData() {
    }

    public static PacketSyncTileData forFieldCommandPost(TileEntityFieldCommandPost commandPost) {
        PacketSyncTileData message = new PacketSyncTileData();
        message.pos = commandPost.getPos();
        message.typeId = TYPE_FIELD_COMMAND_POST;
        message.upgradeLevel = commandPost.getUpgradeLevel();
        message.storedSupplies = commandPost.getStoredSupplies();
        message.maxStoredSupplies = commandPost.getMaxStoredSupplies();
        return message;
    }

    public static PacketSyncTileData forResearchLab(TileEntityResearchLab researchLab) {
        PacketSyncTileData message = new PacketSyncTileData();
        message.pos = researchLab.getPos();
        message.typeId = TYPE_RESEARCH_LAB;
        message.storedSupplies = researchLab.getStoredSupplies();
        message.maxStoredSupplies = researchLab.getMaxStoredSupplies();
        message.researchProgress = researchLab.getResearchProgress();
        message.researchProgressRequired = researchLab.getResearchProgressRequired();
        message.storedSamples = researchLab.getStoredParasiteSamples();
        message.maxStoredSamples = researchLab.getMaxStoredParasiteSamples();
        message.targetResearchId = safeString(researchLab.getTargetResearchId());
        message.targetResearchLabel = safeString(researchLab.getTargetResearchLabel());
        ResearchEntry target = ResearchManager.getResearchEntry(message.targetResearchId);
        if (target != null) {
            message.targetSampleCost = target.getParasiteSampleCost();
            message.targetSupplyCost = target.getSupplyCost();
        }
        return message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        typeId = buf.readInt();
        upgradeLevel = buf.readInt();
        storedSupplies = buf.readInt();
        maxStoredSupplies = buf.readInt();
        researchProgress = buf.readInt();
        researchProgressRequired = buf.readInt();
        storedSamples = buf.readInt();
        maxStoredSamples = buf.readInt();
        targetSampleCost = buf.readInt();
        targetSupplyCost = buf.readInt();
        targetResearchId = ByteBufUtils.readUTF8String(buf);
        targetResearchLabel = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(typeId);
        buf.writeInt(upgradeLevel);
        buf.writeInt(storedSupplies);
        buf.writeInt(maxStoredSupplies);
        buf.writeInt(researchProgress);
        buf.writeInt(researchProgressRequired);
        buf.writeInt(storedSamples);
        buf.writeInt(maxStoredSamples);
        buf.writeInt(targetSampleCost);
        buf.writeInt(targetSupplyCost);
        ByteBufUtils.writeUTF8String(buf, safeString(targetResearchId));
        ByteBufUtils.writeUTF8String(buf, safeString(targetResearchLabel));
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public int getStoredSupplies() {
        return storedSupplies;
    }

    public int getMaxStoredSupplies() {
        return maxStoredSupplies;
    }

    public int getResearchProgress() {
        return researchProgress;
    }

    public int getResearchProgressRequired() {
        return researchProgressRequired;
    }

    public int getStoredSamples() {
        return storedSamples;
    }

    public int getMaxStoredSamples() {
        return maxStoredSamples;
    }

    public int getTargetSampleCost() {
        return targetSampleCost;
    }

    public int getTargetSupplyCost() {
        return targetSupplyCost;
    }

    public String getTargetResearchId() {
        return targetResearchId;
    }

    public String getTargetResearchLabel() {
        return targetResearchLabel;
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    public static final class Handler implements IMessageHandler<PacketSyncTileData, IMessage> {
        @Override
        public IMessage onMessage(final PacketSyncTileData message, MessageContext ctx) {
            StandAndHold.proxy.scheduleClientTask(new Runnable() {
                @Override
                public void run() {
                    StandAndHold.proxy.handleTileDataSync(message);
                }
            });
            return null;
        }
    }
}
