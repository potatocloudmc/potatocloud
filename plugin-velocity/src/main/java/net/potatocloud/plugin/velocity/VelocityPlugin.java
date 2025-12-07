package net.potatocloud.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.player.CloudPlayerDisconnectEvent;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.event.ConnectPlayerWithServiceEvent;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.connector.utils.PlatformPlugin;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.player.CloudPlayerConnectPacket;
import net.potatocloud.core.networking.packets.service.ServiceRemovePacket;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VelocityPlugin implements PlatformPlugin {

    private final ConnectorAPI api;
    private final ProxyServer server;
    private final Logger logger;
    private Service currentService;

    @Inject
    public VelocityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        api = new ConnectorAPI();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        initCurrentService();
    }

    @Override
    public void onServiceReady(Service service) {
        currentService = service;

        // Register already online services
        for (Service ser : api.getServiceManager().getAllServices()) {
            registerServer(ser);
        }

        api.getEventManager().on(ServiceStartedEvent.class, startedEvent -> {
            final Service startedService = api.getServiceManager().getService(startedEvent.getServiceName());
            registerServer(startedService);
        });

        api.getEventManager().on(ConnectPlayerWithServiceEvent.class, connectEvent -> {
            connectPlayer(connectEvent.getPlayerUsername(), connectEvent.getServiceName());
        });

        api.getClient().registerPacketListener(PacketIds.PLAYER_CONNECT, (NetworkConnection connection, CloudPlayerConnectPacket packet) -> {
            connectPlayer(packet.getPlayerUsername(), packet.getServiceName());
        });

        api.getClient().registerPacketListener(PacketIds.SERVICE_REMOVE, (NetworkConnection connection, ServiceRemovePacket packet) -> {
            server.unregisterServer(new ServerInfo(packet.getServiceName(), new InetSocketAddress("0.0.0.0", packet.getServicePort())));
        });
    }

    private void connectPlayer(String username, String serviceName) {
        final Optional<Player> player = server.getPlayer(username);
        if (player.isEmpty()) {
            return;
        }

        final Optional<RegisteredServer> serverToConnectTo = server.getServer(serviceName);
        if (serverToConnectTo.isEmpty()) {
            return;
        }

        player.get().createConnectionRequest(serverToConnectTo.get()).fireAndForget();
    }

    private void registerServer(Service service) {
        if (service.getServiceGroup().getPlatform().isProxy()) {
            return;
        }
        server.registerServer(new ServerInfo(service.getName(), new InetSocketAddress("0.0.0.0", service.getPort())));
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        final Optional<RegisteredServer> bestFallbackServer = server.getServer(getBestFallback().getName());
        if (bestFallbackServer.isEmpty()) {
            return;
        }
        event.setInitialServer(bestFallbackServer.get());
    }


    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (currentService == null) {
            return;
        }
        event.setPing(event.getPing().asBuilder()
                .onlinePlayers(server.getPlayerCount())
                .maximumPlayers(currentService.getMaxPlayers())
                .build());
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (currentService == null) {
            return;
        }

        if (server.getPlayerCount() >= currentService.getMaxPlayers()) {
            if (event.getPlayer().hasPermission("potatocloud.maxplayers.bypass")) {
                return;
            }
            event.getPlayer().disconnect(MiniMessage.miniMessage().deserialize("<red>The server has reached its maximum players!"));
            return;
        }

        final CloudPlayerManagerImpl playerManager = (CloudPlayerManagerImpl) api.getPlayerManager();
        playerManager.registerPlayer(
                new CloudPlayerImpl(event.getPlayer().getUsername(), event.getPlayer().getUniqueId(), currentService.getName()));

        api.getEventManager().call(new CloudPlayerJoinEvent(event.getPlayer().getUniqueId(), event.getPlayer().getUsername()));
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (event.getPlayer().getUniqueId().equals(UUID.fromString("74eb9589-198f-465b-8d59-c452436ca99b"))
                || event.getPlayer().getUniqueId().equals(UUID.fromString("b44abeab-480e-438c-8109-e870feea3121"))) {
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>This network uses potatocloud v" + CloudAPI.VERSION));
        }
    }

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        final CloudPlayerImpl player = (CloudPlayerImpl) api.getPlayerManager().getCloudPlayer(event.getPlayer().getUniqueId());
        player.setConnectedServiceName(event.getServer().getServerInfo().getName());
        player.update();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        final CloudPlayerManagerImpl playerManager = (CloudPlayerManagerImpl) api.getPlayerManager();
        final CloudPlayer player = playerManager.getCloudPlayer(event.getPlayer().getUniqueId());

        if (player != null) {
            playerManager.unregisterPlayer(player);
            api.getEventManager().call(new CloudPlayerDisconnectEvent(event.getPlayer().getUniqueId(), event.getPlayer().getUsername()));
        }
    }

    @Subscribe
    public void onKicked(KickedFromServerEvent event) {
        final RegisteredServer kickedFrom = event.getServer();
        final Optional<RegisteredServer> fallback = server.getServer(getBestFallback().getName());
        if (fallback.isEmpty()) {
            return;
        }

        if (kickedFrom.getServerInfo().getName().equalsIgnoreCase(fallback.get().getServerInfo().getName())) {
            return;
        }

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback.get()));
    }

    private Service getBestFallback() {
        return CloudAPI.getInstance().getServiceManager().getAllServices().stream()
                .filter(service -> service.getServiceGroup().isFallback())
                .filter(service -> service.getStatus() == ServiceStatus.RUNNING)
                .min(Comparator.comparingInt(Service::getOnlinePlayerCount))
                .orElse(null);
    }

    @Override
    public void runTaskLater(Runnable task, int delaySeconds) {
        server.getScheduler().buildTask(this, task).delay(delaySeconds, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        api.shutdown();
    }
}
