package com.liamryan.standandhold.common.block;

import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.item.ModItems;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.supply.SupplyManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public final class BlockSupplyCrate extends Block {
    public BlockSupplyCrate() {
        super(Material.WOOD);
        setRegistryName(StandAndHoldConstants.MOD_ID, "supply_crate");
        setTranslationKey(StandAndHoldConstants.MOD_ID + ".supply_crate");
        setCreativeTab(ModItems.CREATIVE_TAB);
        setHardness(1.2F);
        setResistance(3.0F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote || hand != EnumHand.MAIN_HAND) {
            return true;
        }

        int addedSupplies = SupplyManager.getSupplyCrateValue();
        int totalSupplies = SupplyManager.addSupplies(world, addedSupplies, "supply crate block at " + pos);
        world.setBlockToAir(pos);

        TextComponentTranslation message = new TextComponentTranslation(
                "message.standandhold.supply_crate.claimed",
                addedSupplies,
                totalSupplies
        );
        message.getStyle().setColor(TextFormatting.YELLOW);
        player.sendMessage(message);
        MissionManager.recordSupplyStockpile(world, player, totalSupplies);
        return true;
    }
}
