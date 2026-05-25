package com.liamryan.standandhold.common.gui;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public final class StandAndHoldGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GuiIds.FIELD_COMMAND_POST && tileEntity instanceof TileEntityFieldCommandPost) {
            return new ContainerFieldCommandPost(player.inventory, (TileEntityFieldCommandPost) tileEntity);
        }

        if (id == GuiIds.RESEARCH_LAB && tileEntity instanceof TileEntityResearchLab) {
            return new ContainerResearchLab(player.inventory, (TileEntityResearchLab) tileEntity);
        }

        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return StandAndHold.proxy.getClientGuiElement(id, player, world, new BlockPos(x, y, z));
    }
}
