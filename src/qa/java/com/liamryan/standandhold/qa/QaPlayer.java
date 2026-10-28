package com.liamryan.standandhold.qa;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.MenuProvider;

/** Real server player with the same embedded connection used by vanilla GameTest helpers. */
final class QaPlayer extends ServerPlayer {
    private static final List<QaPlayer> PLAYERS = new ArrayList<>();
    private final List<Component> received = new ArrayList<>();
    private QaPlayer(ServerLevel level, CommonListenerCookie cookie) {
        super(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
    }
    static QaPlayer join(ServerLevel level) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "QAObserver"), false);
        var player = new QaPlayer(level, cookie);
        var connection = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        PLAYERS.add(player);
        return player;
    }
    @Override public void sendSystemMessage(Component message, boolean overlay) { received.add(message.copy()); }
    // Message tests have no negotiated graphical peer; qaClient tests the real menu transport.
    @Override public OptionalInt openMenu(MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) { return OptionalInt.empty(); }
    long messages(String key) {
        return received.stream().filter(c -> c.getContents() instanceof TranslatableContents text && text.getKey().equals(key)).count();
    }
    boolean received(String text) { return received.stream().anyMatch(c -> c.getString().contains(text)); }
    String suggestedCommand(String prefix) {
        for (var message : received.reversed()) {
            if (message.getStyle().getClickEvent() instanceof net.minecraft.network.chat.ClickEvent.SuggestCommand suggestion && suggestion.command().startsWith(prefix)) { return suggestion.command(); }
        }
        throw new IllegalStateException("Expected safe click-to-fill command: " + prefix);
    }
    static void closeAll() {
        for (var player : List.copyOf(PLAYERS)) {
            player.level().getServer().getPlayerList().remove(player);
            player.connection.getConnection().disconnect(Component.literal("QA complete"));
        }
        PLAYERS.clear();
    }
}
