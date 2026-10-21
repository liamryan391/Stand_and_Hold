package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.config.EquipmentConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class PrototypeLauncherItem extends EquipmentItem {
    public PrototypeLauncherItem(Properties properties) { super(properties, EquipmentKind.PROTOTYPE, null); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) { return InteractionResult.SUCCESS; }
        if (player.isSpectator() || !EquipmentAccess.allowed(server, kind)) { EquipmentAccess.notice(player, kind); return InteractionResult.FAIL; }
        var stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack) || stack.isBroken()) { return InteractionResult.FAIL; }
        var bolt = new PrototypeBolt(server, player);
        var direction = player.getLookAngle();
        bolt.shoot(direction.x, direction.y, direction.z, EquipmentConfig.value(EquipmentConfig.SPEED).floatValue(), 0);
        if (!server.addFreshEntity(bolt)) { return InteractionResult.FAIL; }
        player.getCooldowns().addCooldown(stack, EquipmentConfig.value(EquipmentConfig.COOLDOWN));
        stack.hurtAndBreak(1, player, hand);
        server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.45F, 1.3F);
        return InteractionResult.SUCCESS;
    }
}
