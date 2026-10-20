package com.liamryan.standandhold.common.sample;

import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.mission.MissionManager;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class SampleEvents {
    private SampleEvents() { }
    public static void onPickup(ItemEntityPickupEvent.Post event) {
        var original = event.getOriginalStack();
        if (!(event.getPlayer() instanceof ServerPlayer player) || !original.is(SampleItems.TISSUE.get())
                || original.getCount() <= event.getCurrentStack().getCount()) { return; }
        observe(player);
    }
    public static void observe(ServerPlayer player) {
        if (!player.level().getServer().isSameThread()) { return; }
        try {
            var server = player.level().getServer();
            for (String message : MissionManager.observeSamples(HumanWorldData.get(server), FoundationResources.get(server),
                    HumanProgressionConfig.thresholds(), SampleInventory.count(player.getInventory()))) {
                player.sendSystemMessage(Component.literal(message));
            }
        } catch (IllegalArgumentException | IllegalStateException exception) { LogUtils.getLogger().debug("Sample mission observation paused: {}", exception.getMessage()); }
    }
}
