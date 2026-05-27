package com.liamryan.standandhold.common.network;

import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeManager;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.supply.ISupplyStorage;
import com.liamryan.standandhold.common.supply.SupplyTransferManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import com.liamryan.standandhold.common.util.CommandPostMessageHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketGuiAction implements IMessage {
    public static final int ACTION_REQUEST_SYNC = 0;
    public static final int ACTION_COMMAND_POST_UPGRADE = 1;
    public static final int ACTION_RESEARCH_LAB_NEXT = 2;
    public static final int ACTION_RESEARCH_LAB_COMPLETE = 3;
    public static final int ACTION_SUPPLIES_TO_LOCAL = 4;
    public static final int ACTION_SUPPLIES_TO_GLOBAL = 5;

    private int actionId;
    private BlockPos pos = BlockPos.ORIGIN;

    public PacketGuiAction() {
    }

    public PacketGuiAction(int actionId, BlockPos pos) {
        this.actionId = actionId;
        this.pos = pos == null ? BlockPos.ORIGIN : pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        actionId = buf.readInt();
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(actionId);
        buf.writeLong(pos.toLong());
    }

    public int getActionId() {
        return actionId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public static final class Handler implements IMessageHandler<PacketGuiAction, IMessage> {
        @Override
        public IMessage onMessage(final PacketGuiAction message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    handle(message, player);
                }
            });
            return null;
        }

        private void handle(PacketGuiAction message, EntityPlayerMP player) {
            if (player == null || player.world == null || player.world.isRemote) {
                return;
            }

            World world = player.world;
            BlockPos pos = message.getPos();
            if (pos == null || !world.isBlockLoaded(pos) || player.getDistanceSq(pos) > 64.0D) {
                return;
            }

            TileEntity tileEntity = world.getTileEntity(pos);
            switch (message.getActionId()) {
                case ACTION_REQUEST_SYNC:
                    break;
                case ACTION_COMMAND_POST_UPGRADE:
                    handleCommandPostUpgrade(world, pos, tileEntity, player);
                    break;
                case ACTION_RESEARCH_LAB_NEXT:
                    handleResearchLabNext(pos, tileEntity, player);
                    break;
                case ACTION_RESEARCH_LAB_COMPLETE:
                    handleResearchLabComplete(pos, tileEntity, player);
                    break;
                case ACTION_SUPPLIES_TO_LOCAL:
                    handleSupplyTransfer(world, tileEntity, player, true);
                    break;
                case ACTION_SUPPLIES_TO_GLOBAL:
                    handleSupplyTransfer(world, tileEntity, player, false);
                    break;
                default:
                    return;
            }

            StandAndHoldNetwork.sendGuiSync(player, world, pos);
        }

        private void handleCommandPostUpgrade(World world, BlockPos pos, TileEntity tileEntity, EntityPlayerMP player) {
            if (!(tileEntity instanceof TileEntityFieldCommandPost)) {
                return;
            }

            FieldCommandPostUpgradeManager.UpgradeResult result = FieldCommandPostUpgradeManager.tryUpgrade(world, (TileEntityFieldCommandPost) tileEntity, player);
            CommandPostMessageHelper.sendUpgradeResult(player, result);
        }

        private void handleResearchLabNext(BlockPos pos, TileEntity tileEntity, EntityPlayerMP player) {
            if (!(tileEntity instanceof TileEntityResearchLab)) {
                return;
            }

            TileEntityResearchLab lab = (TileEntityResearchLab) tileEntity;
            if (lab.selectNextAvailableResearch()) {
                TextComponentTranslation message = new TextComponentTranslation(
                        "message.standandhold.research_lab.select.success",
                        lab.getTargetResearchLabel()
                );
                message.getStyle().setColor(TextFormatting.AQUA);
                player.sendMessage(message);
            } else {
                TextComponentTranslation message = new TextComponentTranslation("message.standandhold.research_lab.select.none");
                message.getStyle().setColor(TextFormatting.RED);
                player.sendMessage(message);
            }
        }

        private void handleResearchLabComplete(BlockPos pos, TileEntity tileEntity, EntityPlayerMP player) {
            if (!(tileEntity instanceof TileEntityResearchLab)) {
                return;
            }

            TileEntityResearchLab lab = (TileEntityResearchLab) tileEntity;
            if (lab.tryCompleteCurrentResearch()) {
                TextComponentTranslation message = new TextComponentTranslation("message.standandhold.research_lab.complete.success", pos.getX(), pos.getY(), pos.getZ());
                message.getStyle().setColor(TextFormatting.YELLOW);
                player.sendMessage(message);
            } else {
                TextComponentTranslation message = new TextComponentTranslation("message.standandhold.research_lab.complete.not_ready");
                message.getStyle().setColor(TextFormatting.RED);
                player.sendMessage(message);
            }
        }

        private void handleSupplyTransfer(World world, TileEntity tileEntity, EntityPlayerMP player, boolean toLocal) {
            if (!(tileEntity instanceof ISupplyStorage)) {
                return;
            }

            SupplyTransferManager.TransferResult result = toLocal
                    ? SupplyTransferManager.transferToLocal(world, (ISupplyStorage) tileEntity)
                    : SupplyTransferManager.transferToGlobal(world, (ISupplyStorage) tileEntity);
            sendSupplyTransferResult(player, result);
            if (result.getMovedSupplies() > 0) {
                MissionManager.recordSupplyStockpile(world, player, Math.max(result.getGlobalSupplies(), result.getStoredSupplies()));
            }
        }

        private void sendSupplyTransferResult(EntityPlayerMP player, SupplyTransferManager.TransferResult result) {
            TextComponentTranslation message;
            TextFormatting color = TextFormatting.RED;
            switch (result.getStatus()) {
                case TRANSFERRED_TO_LOCAL:
                    color = TextFormatting.YELLOW;
                    message = new TextComponentTranslation(
                            "message.standandhold.supplies.transfer.to_local",
                            result.getMovedSupplies(),
                            result.getStoredSupplies(),
                            result.getMaxStoredSupplies(),
                            result.getGlobalSupplies()
                    );
                    break;
                case TRANSFERRED_TO_GLOBAL:
                    color = TextFormatting.YELLOW;
                    message = new TextComponentTranslation(
                            "message.standandhold.supplies.transfer.to_global",
                            result.getMovedSupplies(),
                            result.getStoredSupplies(),
                            result.getMaxStoredSupplies(),
                            result.getGlobalSupplies()
                    );
                    break;
                case DISABLED:
                    message = new TextComponentTranslation("message.standandhold.supplies.transfer.disabled");
                    break;
                case GLOBAL_EMPTY:
                    message = new TextComponentTranslation("message.standandhold.supplies.transfer.global_empty");
                    break;
                case LOCAL_EMPTY:
                    message = new TextComponentTranslation("message.standandhold.supplies.transfer.local_empty");
                    break;
                case LOCAL_FULL:
                    message = new TextComponentTranslation(
                            "message.standandhold.supplies.transfer.local_full",
                            result.getStoredSupplies(),
                            result.getMaxStoredSupplies()
                    );
                    break;
                default:
                    message = new TextComponentTranslation("message.standandhold.supplies.transfer.no_storage");
                    break;
            }

            message.getStyle().setColor(color);
            player.sendMessage(message);
        }
    }
}
