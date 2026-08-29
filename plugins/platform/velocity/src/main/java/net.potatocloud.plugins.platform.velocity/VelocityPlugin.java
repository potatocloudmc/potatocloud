package net.potatocloud.plugins.platform.velocity;

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
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.event.ConnectPlayerWithServiceEvent;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.connector.utils.PlatformPlugin;
import net.potatocloud.network.packets.player.CloudPlayerConnectPacket;
import net.potatocloud.network.packets.service.ServiceRemovePacket;
import net.potatocloud.network.packets.service.ServiceStartedPacket;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityPlugin implements PlatformPlugin {

    private final ConnectorAPI api;
    private final ProxyServer server;
    private Service currentService;

    @Inject
    public VelocityPlugin(ProxyServer server) {
        this.server = server;
        api = new ConnectorAPI();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        initCurrentService();
    }

    @Override
    public void onServiceReady(Service service) {
        currentService = service;

        // register services that are already running
        api.serviceManager().services().forEach(this::registerServer);

        api.client().on(ServiceStartedPacket.class, ctx ->
                api.serviceManager().find(ctx.packet().serviceName()).ifPresent(this::registerServer));

        api.eventBus().subscribe(ConnectPlayerWithServiceEvent.class, connectEvent ->
                connectPlayer(connectEvent.playerUsername(), connectEvent.serviceName()));

        api.client().on(CloudPlayerConnectPacket.class, ctx ->
                connectPlayer(ctx.packet().playerUsername(), ctx.packet().serviceName()));

        api.client().on(ServiceRemovePacket.class, ctx ->
                server.unregisterServer(new ServerInfo(ctx.packet().serviceName(), new InetSocketAddress(ctx.packet().serviceHost(), ctx.packet().servicePort()))));
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
        if (service == null) {
            return;
        }

        if (server.getServer(service.name()).isPresent()) {
            return;
        }

        if (service.group().platform().proxy()) {
            return;
        }

        server.registerServer(new ServerInfo(service.name(), new InetSocketAddress(service.host(), service.port())));
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        final Service bestFallback = getBestFallback();
        if (bestFallback == null) {
            return;
        }

        final Optional<RegisteredServer> fallback = server.getServer(bestFallback.name());
        if (fallback.isEmpty()) {
            return;
        }

        event.setInitialServer(fallback.get());
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (currentService == null) {
            return;
        }
        event.setPing(event.getPing().asBuilder()
                .onlinePlayers(server.getPlayerCount())
                .maximumPlayers(currentService.maxPlayers())
                .build());
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (currentService == null) {
            return;
        }

        if (server.getPlayerCount() >= currentService.maxPlayers()) {
            if (event.getPlayer().hasPermission("potatocloud.maxplayers.bypass")) {
                return;
            }
            event.getPlayer().disconnect(MiniMessage.miniMessage().deserialize("<red>The server has reached its maximum players!"));
            return;
        }

        final CloudPlayerManagerImpl playerManager = (CloudPlayerManagerImpl) api.playerManager();
        playerManager.registerPlayer(
                new CloudPlayerImpl(event.getPlayer().getUsername(), event.getPlayer().getUniqueId(), currentService.name()));

        api.eventBus().publish(new CloudPlayerJoinEvent(event.getPlayer().getUniqueId(), event.getPlayer().getUsername()));
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

    @Subscribe
    public void onKicked(KickedFromServerEvent event) {
        final RegisteredServer kickedFrom = event.getServer();
        final Service bestFallback = getBestFallback();
        if (bestFallback == null) {
            return;
        }

        final Optional<RegisteredServer> fallback = server.getServer(bestFallback.name());
        if (fallback.isEmpty()) {
            return;
        }

        if (kickedFrom.getServerInfo().getName().equalsIgnoreCase(fallback.get().getServerInfo().getName())) {
            return;
        }

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback.get()));
    }

    private Service getBestFallback() {
        return CloudAPI.instance().serviceManager().services().stream()
                .filter(service -> service.group() != null && service.group().fallback())
                .filter(service -> service.state() == ServiceState.RUNNING)
                .min(Comparator.comparingInt(Service::playerCount))
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
