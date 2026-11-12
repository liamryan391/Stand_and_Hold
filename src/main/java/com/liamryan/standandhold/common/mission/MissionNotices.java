package com.liamryan.standandhold.common.mission;

import com.liamryan.standandhold.common.building.BuildingPositions;
import com.liamryan.standandhold.common.command.CommandGuide;
import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationResources;
import com.liamryan.standandhold.common.sample.SampleInventory;
import com.liamryan.standandhold.common.world.HumanWorldData;
import com.liamryan.standandhold.config.HumanProgressionConfig;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Delivers {@link MissionManager#sync} results. A completion changes shared world state, so one short line reaches
 * every online player once. Start instructions and progress go to the acting player; with no actor, a start is one
 * short server line and the Orders tab is the place to catch up. Server thread only; no ticking, polling or world scans.
 */
public final class MissionNotices {
    public static final int NOT_OBSERVED = -1;
    private MissionNotices() { }

    /** {@code actor} may be null for building ticks; {@code explicit} reports blocked updates to the actor (menus, use). */
    public static void sync(MinecraftServer server, ServerPlayer actor, int carried, boolean explicit) {
        if (server == null || !server.isSameThread()) { return; }
        try {
            FoundationCatalog catalog = FoundationResources.get(server);
            long[] thresholds = HumanProgressionConfig.thresholds();
            HumanWorldData data = HumanWorldData.get(server);
            deliver(server, actor, MissionManager.sync(data, catalog, thresholds, carried, () -> BuildingPositions.anyLivePost(server, data)),
                    catalog, thresholds, explicit);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            if (explicit && actor != null) { actor.sendSystemMessage(Component.translatable("message.standandhold.orders_unavailable", String.valueOf(exception.getMessage()))); }
            else { LogUtils.getLogger().debug("Order update paused: {}", exception.getMessage()); }
        }
    }

    /** Observes the player's own carried samples, so samples collected before an order started still count once. */
    public static void observe(ServerPlayer player, boolean explicit) {
        sync(player.level().getServer(), player, SampleInventory.count(player.getInventory()), explicit);
    }

    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { observe(player, false); }
    }

    public static void deliver(MinecraftServer server, ServerPlayer actor, List<MissionManager.Notice> notices,
                               FoundationCatalog catalog, long[] thresholds, boolean explicit) {
        for (var notice : notices) {
            switch (notice.change()) {
                case STARTED -> {
                    if (actor != null) {
                        actor.sendSystemMessage(Component.literal(notice.text()).withStyle(ChatFormatting.GOLD));
                        actor.sendSystemMessage(Component.literal(hint(notice.mission(), catalog, thresholds)).withStyle(ChatFormatting.GRAY));
                    } else {
                        // No acting player (building ticks): one short line; the Orders tab carries the instructions.
                        broadcast(server, Component.literal(notice.text() + ". Details: any Command Post, Orders tab.").withStyle(ChatFormatting.GOLD));
                    }
                }
                case COMPLETED -> broadcast(server, Component.literal(notice.text()).withStyle(ChatFormatting.GREEN));
                case PROGRESS -> { if (actor != null) { actor.sendSystemMessage(Component.literal(notice.text())); } }
                case BLOCKED -> {
                    if (explicit && actor != null) { actor.sendSystemMessage(Component.literal(notice.text())); }
                    else { LogUtils.getLogger().debug(notice.text()); }
                }
            }
        }
    }

    private static String hint(Mission mission, FoundationCatalog catalog, long[] thresholds) {
        try { return CommandGuide.hint(mission, catalog, thresholds) + " Details: any Command Post, Orders tab."; }
        catch (IllegalArgumentException | IllegalStateException exception) { return "Details: any Command Post, Orders tab."; }
    }

    private static void broadcast(MinecraftServer server, Component message) {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
