package net.potatocloud.plugins.platform.spigot.listener;

import net.potatocloud.connector.utils.ServiceConnector;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

public final class MaxPlayersListener implements Listener {

    private final ServiceConnector connector;

    public MaxPlayersListener(ServiceConnector connector) {
        this.connector = connector;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        connector.currentService().ifPresent(service -> event.setMaxPlayers(service.maxPlayers()));
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        connector.currentService()
                .filter(service -> Bukkit.getOnlinePlayers().size() >= service.maxPlayers())
                .filter(_ -> !event.getPlayer().hasPermission(ServiceConnector.MAX_PLAYERS_BYPASS_PERMISSION))
                .ifPresent(_ -> event.disallow(PlayerLoginEvent.Result.KICK_FULL, "§c" + ServiceConnector.SERVER_FULL_MESSAGE));
    }
}
