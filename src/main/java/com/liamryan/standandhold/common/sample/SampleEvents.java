package com.liamryan.standandhold.common.sample;

import com.liamryan.standandhold.common.mission.MissionNotices;
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
    /** Highest carried count only; also starts eligible onboarding orders so earlier-collected samples count once. */
    public static void observe(ServerPlayer player) { MissionNotices.observe(player, false); }
}
