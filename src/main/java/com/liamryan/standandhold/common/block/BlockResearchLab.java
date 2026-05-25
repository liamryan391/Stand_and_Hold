package com.liamryan.standandhold.common.block;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.common.progression.HumanPointManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import com.liamryan.standandhold.common.tile.TileEntityResearchLab;
import com.liamryan.standandhold.common.world.HumanWorldData;
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

public final class BlockResearchLab extends Block implements ITileEntityProvider {
    public BlockResearchLab() {
        super(Material.IRON);
        setRegistryName(StandAndHoldConstants.MOD_ID, "research_lab");
        setTranslationKey(StandAndHoldConstants.MOD_ID + ".research_lab");
        setCreativeTab(ModItems.CREATIVE_TAB);
        setHardness(2.0F);
        setResistance(6.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityResearchLab();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityResearchLab();
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote) {
            HumanPointManager.getData(world).registerResearchLab(world.provider.getDimension(), pos);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            HumanPointManager.getData(world).unregisterResearchLab(world.provider.getDimension(), pos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote || hand != EnumHand.MAIN_HAND) {
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityResearchLab)) {
            return true;
        }

        TileEntityResearchLab lab = (TileEntityResearchLab) tileEntity;
        ItemStack heldStack = player.getHeldItem(hand);
        if (!heldStack.isEmpty() && heldStack.getItem() == Item.getItemFromBlock(ModBlocks.SUPPLY_CRATE)) {
            int requestedSupplies = SupplyManager.getSupplyCrateValue();
            int acceptedSupplies = lab.addStoredSupplies(requestedSupplies);
            if (acceptedSupplies <= 0) {
                TextComponentTranslation message = new TextComponentTranslation(
                        "message.standandhold.research_lab.supplies_full",
                        lab.getStoredSupplies(),
                        lab.getMaxStoredSupplies()
                );
                message.getStyle().setColor(TextFormatting.RED);
                player.sendMessage(message);
                return true;
            }

            int totalSupplies = SupplyManager.addSupplies(world, acceptedSupplies, "supply crate deposited into research lab: " + pos);
            if (!player.capabilities.isCreativeMode) {
                heldStack.shrink(1);
                player.inventory.markDirty();
            }

            TextComponentTranslation message = new TextComponentTranslation(
                    "message.standandhold.research_lab.supply_inserted",
                    acceptedSupplies,
                    lab.getStoredSupplies(),
                    lab.getMaxStoredSupplies(),
                    totalSupplies
            );
            message.getStyle().setColor(TextFormatting.YELLOW);
            player.sendMessage(message);
            return true;
        }

        if (!heldStack.isEmpty() && heldStack.getItem() == ModItems.PARASITE_TISSUE_SAMPLE) {
            if (!lab.addStoredParasiteSamples(1)) {
                TextComponentTranslation message = new TextComponentTranslation(
                        "message.standandhold.research_lab.samples_full",
                        lab.getStoredParasiteSamples(),
                        lab.getMaxStoredParasiteSamples()
                );
                message.getStyle().setColor(TextFormatting.RED);
                player.sendMessage(message);
                return true;
            }

            if (!player.capabilities.isCreativeMode) {
                heldStack.shrink(1);
                player.inventory.markDirty();
            }

            lab.tryCompleteCurrentResearch();
            TextComponentTranslation message = new TextComponentTranslation(
                    "message.standandhold.research_lab.sample_inserted",
                    lab.getStoredParasiteSamples(),
                    lab.getMaxStoredParasiteSamples()
            );
            message.getStyle().setColor(TextFormatting.YELLOW);
            player.sendMessage(message);
            return true;
        }

        sendStatus(player, lab);
        return true;
    }

    private void sendStatus(EntityPlayer player, TileEntityResearchLab lab) {
        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.research_lab.status",
                lab.getTargetResearchLabel(),
                lab.getResearchProgress(),
                lab.getResearchProgressRequired(),
                lab.getStoredParasiteSamples(),
                lab.getMaxStoredParasiteSamples(),
                lab.getStoredSupplies(),
                lab.getMaxStoredSupplies(),
                HumanPointManager.getData(player.world).getSupplyPoints()
        );
        message.getStyle().setColor(TextFormatting.AQUA);
        player.sendMessage(message);
    }
}
