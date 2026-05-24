package com.liamryan.standandhold.common.block;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.progression.HumanStage;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.common.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class BlockFieldCommandPost extends Block implements ITileEntityProvider {
    public BlockFieldCommandPost() {
        super(Material.IRON);
        setRegistryName(StandAndHoldConstants.MOD_ID, "field_command_post");
        setTranslationKey(StandAndHoldConstants.MOD_ID + ".field_command_post");
        setCreativeTab(ModItems.CREATIVE_TAB);
        setHardness(2.5F);
        setResistance(8.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityFieldCommandPost();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityFieldCommandPost();
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote) {
            HumanPointManager.getData(world).registerFieldCommandPost(world.provider.getDimension(), pos);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            HumanPointManager.getData(world).unregisterFieldCommandPost(world.provider.getDimension(), pos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        HumanWorldData data = HumanPointManager.getData(world);
        HumanStage stage = data.getStage();
        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.field_command_post.status",
                data.getHumanPoints(),
                stage.getId(),
                stage.getDisplayName()
        );
        message.getStyle().setColor(TextFormatting.GREEN);
        player.sendMessage(message);
        return true;
    }
}
