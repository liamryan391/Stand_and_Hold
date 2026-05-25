package com.liamryan.standandhold.common.block;

import com.liamryan.standandhold.StandAndHold;
import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.gui.GuiIds;
import com.liamryan.standandhold.common.infrastructure.FieldCommandPostUpgradeManager;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.tile.TileEntityFieldCommandPost;
import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.common.util.CommandPostMessageHelper;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        if (world.isRemote || hand != EnumHand.MAIN_HAND) {
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        TileEntityFieldCommandPost commandPost = tileEntity instanceof TileEntityFieldCommandPost ? (TileEntityFieldCommandPost) tileEntity : null;
        ItemStack heldStack = player.getHeldItem(hand);
        if (commandPost != null && isSupplyCrate(heldStack)) {
            sendSupplyDepositResult(world, player, commandPost, heldStack);
            return true;
        }

        if (commandPost != null && player.isSneaking()) {
            CommandPostMessageHelper.sendUpgradeResult(player, FieldCommandPostUpgradeManager.tryUpgrade(world, commandPost, player));
            return true;
        }

        player.openGui(StandAndHold.instance, GuiIds.FIELD_COMMAND_POST, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    private boolean isSupplyCrate(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(ModBlocks.SUPPLY_CRATE);
    }

    private void sendSupplyDepositResult(World world, EntityPlayer player, TileEntityFieldCommandPost commandPost, ItemStack heldStack) {
        int requestedSupplies = SupplyManager.getSupplyCrateValue();
        int acceptedSupplies = commandPost.addStoredSupplies(requestedSupplies);
        if (acceptedSupplies <= 0) {
            TextComponentTranslation message = new TextComponentTranslation(
                    "message.standandhold.field_command_post.supplies_full",
                    commandPost.getStoredSupplies(),
                    commandPost.getMaxStoredSupplies()
            );
            message.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(message);
            return;
        }

        if (!player.capabilities.isCreativeMode) {
            heldStack.shrink(1);
            player.inventory.markDirty();
        }

        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.field_command_post.supply_inserted",
                acceptedSupplies,
                commandPost.getStoredSupplies(),
                commandPost.getMaxStoredSupplies()
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        player.sendMessage(message);
    }
}
