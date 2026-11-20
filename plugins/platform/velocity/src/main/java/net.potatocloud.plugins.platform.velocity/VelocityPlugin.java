package net.potatocloud.plugins.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.event.ConnectPlayerWithServiceEvent;
import net.potatocloud.connector.utils.ServiceConnector;
import net.potatocloud.network.packets.player.CloudPlayerConnectPacket;
import net.potatocloud.network.packets.player.CloudPlayerKickPacket;
import net.potatocloud.network.packets.service.ServiceRemovePacket;
import net.potatocloud.network.packets.service.ServiceStartedPacket;
import net.potatocloud.plugins.platform.velocity.listener.CloudPlayerListener;
import net.potatocloud.plugins.platform.velocity.listener.FallbackListener;
import net.potatocloud.plugins.platform.velocity.listener.MaintenanceListener;
import net.potatocloud.plugins.platform.velocity.listener.MaxPlayersListener;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class VelocityPlugin {

    private final ProxyServer server;
    private final ServiceConnector connector;
    private final ConnectorAPI api;

    @Inject
    public VelocityPlugin(ProxyServer server) {
        this.server = server;
        this.connector = new ServiceConnector();
        this.api = connector.api();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        connector.initCurrentService();

        final EventManager eventManager = server.getEventManager();
        eventManager.register(this, new MaxPlayersListener(server, connector));
        eventManager.register(this, new MaintenanceListener(connector));
        eventManager.register(this, new CloudPlayerListener(connector));
        eventManager.register(this, new FallbackListener(server, api));

        // register services that are already running
        api.serviceManager().services().forEach(this::registerServer);

        api.client().on(ServiceStartedPacket.class, ctx ->
                api.serviceManager().find(ctx.packet().serviceName()).ifPresent(this::registerServer));

        api.eventBus().subscribe(ConnectPlayerWithServiceEvent.class, connectEvent ->
                connectPlayer(connectEvent.playerUsername(), connectEvent.serviceName()));

        api.client().on(CloudPlayerConnectPacket.class, ctx ->
                connectPlayer(ctx.packet().playerUsername(), ctx.packet().serviceName()));

        api.client().on(CloudPlayerKickPacket.class, ctx ->
                server.getPlayer(ctx.packet().playerUsername()).ifPresent(player ->
                        player.disconnect(MiniMessage.miniMessage().deserialize(ctx.packet().reason()))));

        api.client().on(ServiceRemovePacket.class, ctx ->
                server.unregisterServer(new ServerInfo(ctx.packet().serviceName(), new InetSocketAddress(ctx.packet().serviceHost(), ctx.packet().servicePort()))));
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

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        connector.shutdown();
    }
}
