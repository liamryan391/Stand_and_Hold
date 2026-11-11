package com.liamryan.standandhold.common.npc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;

/** Creative test access only; not a survival recruit or a spawner configuration item. */
final class SpecialistEgg extends SpawnEggItem {
    SpecialistEgg(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.isCreative()
                || context.getLevel().getBlockEntity(context.getClickedPos()) instanceof Spawner) { return InteractionResult.FAIL; }
        return super.useOn(context);
    }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return player.isCreative() ? super.use(level, player, hand) : InteractionResult.FAIL;
    }
}
