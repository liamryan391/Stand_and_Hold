package com.liamryan.standandhold.common.network;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import com.liamryan.standandhold.common.world.HumanWorldData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class StandAndHoldNetwork {
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(StandAndHoldConstants.MOD_ID);
    private static int discriminator;
    private static boolean registered;

    private StandAndHoldNetwork() {
    }

    public static void registerMessages() {
        if (registered) {
            return;
        }

        CHANNEL.registerMessage(PacketSyncHumanProgression.Handler.class, PacketSyncHumanProgression.class, discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(PacketSyncTileData.Handler.class, PacketSyncTileData.class, discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(PacketGuiAction.Handler.class, PacketGuiAction.class, discriminator++, Side.SERVER);
        registered = true;
    }

    public static void sendToServer(IMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendGuiSync(EntityPlayerMP player, World world, BlockPos pos) {
        if (player == null || world == null || world.isRemote) {
            return;
        }

        sendProgressionSync(player, world);
        sendTileSync(player, world, pos);
    }

    public static void sendProgressionSync(EntityPlayerMP player, World world) {
        if (player == null || world == null || world.isRemote) {
            return;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        CHANNEL.sendTo(new PacketSyncHumanProgression(data.getHumanPoints(), data.getStage().getId(), data.getSupplyPoints()), player);
    }

    public static void sendTileSync(EntityPlayerMP player, World world, BlockPos pos) {
        if (player == null || world == null || world.isRemote || pos == null || !world.isBlockLoaded(pos)) {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        PacketSyncTileData message = null;
        if (tileEntity instanceof TileEntityFieldCommandPost) {
            message = PacketSyncTileData.forFieldCommandPost((TileEntityFieldCommandPost) tileEntity);
        } else if (tileEntity instanceof TileEntityResearchLab) {
            message = PacketSyncTileData.forResearchLab((TileEntityResearchLab) tileEntity);
        }

        if (message != null) {
            CHANNEL.sendTo(message, player);
        }
    }
}
