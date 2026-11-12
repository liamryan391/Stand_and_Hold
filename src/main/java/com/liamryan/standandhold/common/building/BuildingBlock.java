package com.liamryan.standandhold.common.building;

import com.liamryan.standandhold.common.mission.MissionNotices;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.BuildingConfig;
import com.liamryan.standandhold.config.StructureConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class BuildingBlock extends BaseEntityBlock {
    private final BuildingKind kind;
    public BuildingBlock(BuildingKind kind, Properties properties) { super(properties); this.kind = kind; }
    public BuildingKind kind() { return kind; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BuildingBlockEntity(pos, state); }
    /** Item placement only: refuses a Command Post too close to another post. The item is not consumed. */
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (kind == BuildingKind.COMMAND_POST && context.getLevel() instanceof ServerLevel server) {
            try {
                int spacing = BuildingConfig.POST_SPACING.get();
                var close = PostSpacing.tooClose(HumanWorldData.get(server.getServer()), server.dimension().identifier(), context.getClickedPos(), spacing);
                if (close.isPresent()) {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        player.sendSystemMessage(Component.translatable("message.standandhold.post_too_close", close.getAsInt(), spacing));
                    }
                    return null;
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                LogUtils.getLogger().debug("Command Post spacing check skipped: {}", exception.getMessage());
            }
        }
        return super.getStateForPlacement(context);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !player.isSpectator() && level.getBlockEntity(pos) instanceof BuildingBlockEntity building) {
            try {
                if (level instanceof ServerLevel server && building.isMainBase()) {
                    var data = HumanWorldData.get(server.getServer());
                    data.discoverMainBase(new BuildingPosition(server.dimension().identifier(), pos));
                }
                // Opening any building observes the opener's carried samples once and starts eligible onboarding orders.
                if (player instanceof ServerPlayer actor) { MissionNotices.observe(actor, true); }
                if (level instanceof ServerLevel server && building.isMainBase()) {
                    var data = HumanWorldData.get(server.getServer());
                    boolean active = building.operational(data);
                    if (building.claimMainBaseNotice(active)) {
                        player.sendSystemMessage(active ? Component.translatable("message.standandhold.main_base_active")
                                : Component.translatable("message.standandhold.main_base_dormant", StructureConfig.ACTIVATION_STAGE.get()));
                    }
                }
                player.openMenu(building, building::writeMenuData);
            }
            catch (IllegalArgumentException | IllegalStateException exception) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(exception.getMessage()));
            }
        }
        return InteractionResult.SUCCESS;
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() || kind == BuildingKind.CRATE ? null
                : createTickerHelper(type, BuildingRegistry.ENTITY.get(), BuildingBlockEntity::tick);
    }
}
