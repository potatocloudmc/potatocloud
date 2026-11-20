package net.potatocloud.plugins.platform.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.player.CloudPlayerDisconnectEvent;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.connector.utils.ServiceConnector;

import java.util.UUID;

public final class CloudPlayerListener {

    private final ServiceConnector connector;
    private final ConnectorAPI api;

    public CloudPlayerListener(ServiceConnector connector) {
        this.connector = connector;
        this.api = connector.api();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        connector.currentService().ifPresent(service -> {
            final CloudPlayerManagerImpl playerManager = (CloudPlayerManagerImpl) api.playerManager();

            playerManager.registerPlayer(new CloudPlayerImpl(event.getPlayer().getUsername(), event.getPlayer().getUniqueId(), service.name()));

            api.eventBus().publish(new CloudPlayerJoinEvent(event.getPlayer().getUniqueId(), event.getPlayer().getUsername()));
        });

        if (event.getPlayer().getUniqueId().equals(UUID.fromString("74eb9589-198f-465b-8d59-c452436ca99b"))
                || event.getPlayer().getUniqueId().equals(UUID.fromString("b44abeab-480e-438c-8109-e870feea3121"))) {
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>This network uses potatocloud v" + CloudAPI.VERSION));
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        api.playerManager().find(event.getPlayer().getUniqueId()).ifPresent(player -> {
            if (player instanceof CloudPlayerImpl playerImpl) {
                playerImpl.serviceName(event.getServer().getServerInfo().getName());
                api.playerManager().update(player);
            }
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        final CloudPlayerManagerImpl playerManager = (CloudPlayerManagerImpl) api.playerManager();

        playerManager.find(event.getPlayer().getUniqueId()).ifPresent(player -> {
            playerManager.unregisterPlayer(player);
            api.eventBus().publish(new CloudPlayerDisconnectEvent(event.getPlayer().getUniqueId(), event.getPlayer().getUsername()));
        });
    }
}
