package com.blazesmp.rank;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class RankListener implements Listener {
    private final RankService ranks;

    public RankListener(RankService ranks) {
        this.ranks = ranks;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ranks.apply(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ranks.remove(event.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Rank rank = ranks.getRank(event.getPlayer());
        event.renderer((source, sourceDisplayName, message, viewer) ->
                Component.textOfChildren(
                        Component.text("[" + rank.displayName() + "] ", rank.color()),
                        Component.text(source.getName()),
                        Component.text(" » "),
                        message
                ));
    }
}
