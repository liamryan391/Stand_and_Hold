package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.common.building.BuildingBlockEntity;
import com.liamryan.standandhold.common.building.BuildingPosition;
import com.liamryan.standandhold.config.ResponseConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * M35: a raid bar at the top of the screen for players near a post, like vanilla raids. Yellow and counting down while a
 * raid is warned; red with the raiders left while it is on. Built on Minecraft's own boss bar; nothing is saved (the
 * saved raid record is the truth, so a restart rebuilds the bar on the post's next update).
 */
public final class RaidBars {
    private static final Map<MinecraftServer, Map<String, Bar>> BARS = new HashMap<>();
    private static final int RANGE_SQUARED = 64 * 64;
    private record Bar(ServerBossEvent event, int[] most) { }
    private RaidBars() { }

    /** Called about once a second by each loaded Command Post. Cost: one record read and the nearby players. */
    public static void update(ServerLevel level, BuildingBlockEntity post) {
        String key = new BuildingPosition(level.dimension().identifier(), post.getBlockPos()).key();
        var record = ThreatWorldData.get(level.getServer()).response(key);
        long now = level.getGameTime();
        if (record == null || !record.hasPending() && record.raiders().isEmpty()) { remove(level.getServer(), key); return; }
        var bars = BARS.computeIfAbsent(level.getServer(), server -> new HashMap<>());
        var bar = bars.computeIfAbsent(key, ignored -> new Bar(new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS), new int[1]));
        String where = post.getBlockPos().toShortString();
        if (record.hasPending()) {
            int lead = Math.max(1, ResponseConfig.WARNING_TICKS.get());
            bar.event().setName(Component.translatable("bossbar.standandhold.raid_warning", where, Component.translatable("direction.standandhold." + record.bearing())));
            bar.event().setColor(BossEvent.BossBarColor.YELLOW);
            bar.event().setProgress(Math.clamp((record.pendingRaid() - now) / (float) lead, 0F, 1F));
            bar.most()[0] = 0;
        } else {
            bar.most()[0] = Math.max(bar.most()[0], record.raiders().size());
            bar.event().setName(Component.translatable("bossbar.standandhold.raid", where, record.raiders().size()));
            bar.event().setColor(BossEvent.BossBarColor.RED);
            bar.event().setProgress(Math.clamp(record.raiders().size() / (float) Math.max(1, bar.most()[0]), 0F, 1F));
        }
        for (ServerPlayer player : level.players()) {
            boolean near = !player.isSpectator() && player.blockPosition().distSqr(post.getBlockPos()) <= RANGE_SQUARED;
            if (near) { bar.event().addPlayer(player); } else { bar.event().removePlayer(player); }
        }
        for (ServerPlayer shown : java.util.List.copyOf(bar.event().getPlayers())) {
            if (shown.level() != level || shown.hasDisconnected()) { bar.event().removePlayer(shown); }
        }
    }
    public static void remove(MinecraftServer server, String key) {
        var bars = BARS.get(server);
        if (bars == null) { return; }
        var bar = bars.remove(key);
        if (bar != null) { bar.event().removeAllPlayers(); }
    }
    /** Test and status access: the live bar for a post, if any. */
    public static Optional<ServerBossEvent> bar(MinecraftServer server, String key) {
        return Optional.ofNullable(BARS.getOrDefault(server, Map.of()).get(key)).map(Bar::event);
    }
    public static void stopped(ServerStoppedEvent event) {
        var bars = BARS.remove(event.getServer());
        if (bars != null) { bars.values().forEach(bar -> bar.event().removeAllPlayers()); }
    }
}
