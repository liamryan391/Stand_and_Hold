package com.liamryan.standandhold.client;

import com.liamryan.standandhold.client.gui.GuiFieldCommandPost;
import com.liamryan.standandhold.client.gui.GuiResearchLab;
import com.liamryan.standandhold.client.render.ModEntityRenderers;
import com.liamryan.standandhold.common.gui.ContainerFieldCommandPost;
import com.liamryan.standandhold.common.gui.ContainerResearchLab;
import com.liamryan.standandhold.common.gui.GuiIds;
import com.liamryan.standandhold.common.network.PacketSyncHumanProgression;
import com.liamryan.standandhold.common.network.PacketSyncTileData;
import com.liamryan.standandhold.common.proxy.CommonProxy;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ModEntityRenderers.registerEntityRenderers();
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (id == GuiIds.FIELD_COMMAND_POST && tileEntity instanceof TileEntityFieldCommandPost) {
            return new GuiFieldCommandPost(new ContainerFieldCommandPost(player.inventory, (TileEntityFieldCommandPost) tileEntity));
        }

        if (id == GuiIds.RESEARCH_LAB && tileEntity instanceof TileEntityResearchLab) {
            return new GuiResearchLab(new ContainerResearchLab(player.inventory, (TileEntityResearchLab) tileEntity));
        }

        return null;
    }

    @Override
    public void scheduleClientTask(Runnable task) {
        Minecraft.getMinecraft().addScheduledTask(task);
    }

    @Override
    public void handleHumanProgressionSync(PacketSyncHumanProgression message) {
        ClientSyncedData.setProgression(message.getHumanPoints(), message.getStageId(), message.getSupplyPoints());
    }

    @Override
    public void handleTileDataSync(PacketSyncTileData message) {
        ClientSyncedData.setTileData(message);
    }
}
