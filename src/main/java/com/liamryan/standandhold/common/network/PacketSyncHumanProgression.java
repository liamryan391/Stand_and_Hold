package com.liamryan.standandhold.common.network;

import com.liamryan.standandhold.StandAndHold;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketSyncHumanProgression implements IMessage {
    private int humanPoints;
    private int stageId;
    private int supplyPoints;

    public PacketSyncHumanProgression() {
    }

    public PacketSyncHumanProgression(int humanPoints, int stageId, int supplyPoints) {
        this.humanPoints = Math.max(0, humanPoints);
        this.stageId = Math.max(0, stageId);
        this.supplyPoints = Math.max(0, supplyPoints);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        humanPoints = buf.readInt();
        stageId = buf.readInt();
        supplyPoints = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(humanPoints);
        buf.writeInt(stageId);
        buf.writeInt(supplyPoints);
    }

    public int getHumanPoints() {
        return humanPoints;
    }

    public int getStageId() {
        return stageId;
    }

    public int getSupplyPoints() {
        return supplyPoints;
    }

    public static final class Handler implements IMessageHandler<PacketSyncHumanProgression, IMessage> {
        @Override
        public IMessage onMessage(final PacketSyncHumanProgression message, MessageContext ctx) {
            StandAndHold.proxy.scheduleClientTask(new Runnable() {
                @Override
                public void run() {
                    StandAndHold.proxy.handleHumanProgressionSync(message);
                }
            });
            return null;
        }
    }
}
