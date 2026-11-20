package net.potatocloud.plugins.platform.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.connector.utils.ServiceConnector;

public final class MaxPlayersListener {

    private final ProxyServer server;
    private final ServiceConnector connector;

    public MaxPlayersListener(ProxyServer server, ServiceConnector connector) {
        this.server = server;
        this.connector = connector;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        connector.currentService().ifPresent(service -> event.setPing(event.getPing().asBuilder()
                .onlinePlayers(server.getPlayerCount())
                .maximumPlayers(service.maxPlayers())
                .build()));
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        connector.currentService()
                .filter(service -> server.getPlayerCount() >= service.maxPlayers())
                .filter(_ -> !event.getPlayer().hasPermission(ServiceConnector.MAX_PLAYERS_BYPASS_PERMISSION))
                .ifPresent(_ -> event.getPlayer().disconnect(MiniMessage.miniMessage().deserialize("<red>" + ServiceConnector.SERVER_FULL_MESSAGE)));
    }
}
