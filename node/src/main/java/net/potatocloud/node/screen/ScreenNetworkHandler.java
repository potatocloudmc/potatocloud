package net.potatocloud.node.screen;

import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.service.RequestServiceLogsPacket;
import net.potatocloud.network.packets.service.ServiceLogsResponsePacket;
import net.potatocloud.network.packets.service.ServiceScreenLogPacket;
import net.potatocloud.network.packets.service.ServiceScreenSubscribePacket;
import net.potatocloud.network.packets.service.ServiceScreenUnsubscribePacket;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.node.cluster.ClusterManagerImpl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ScreenNetworkHandler {

    private final ScreenManager screens;
    private final ClusterManagerImpl clusterManager;
    private final Map<NetworkConnection, Set<String>> subscriptions = new ConcurrentHashMap<>();

    public ScreenNetworkHandler(ScreenManager screens, NetworkServer server, ClusterManagerImpl clusterManager) {
        this.screens = screens;
        this.clusterManager = clusterManager;

        server.on(ServiceScreenSubscribePacket.class, ctx -> subscribe(ctx.connection(), ctx.packet().serviceName()));
        server.on(ServiceScreenUnsubscribePacket.class, ctx -> unsubscribe(ctx.connection(), ctx.packet().serviceName()));
        server.on(ServiceScreenLogPacket.class, ctx -> screens.append(ctx.packet().serviceName(), ctx.packet().line()));
        server.on(RequestServiceLogsPacket.class, this::sendLogs);
        server.onClientDisconnected(this::unsubscribeAll);
    }

    public void screenOpened(Screen screen) {
        if (screen.isRemote()) {
            clusterManager.sendTo(screen.remoteNode(), new ServiceScreenSubscribePacket(screen.name()));
        }
    }

    public void screenClosed(Screen screen) {
        if (screen.isRemote()) {
            clusterManager.sendTo(screen.remoteNode(), new ServiceScreenUnsubscribePacket(screen.name()));
        }
    }

    public void sendLog(String screenName, String line) {
        subscriptions.forEach((connection, screenNames) -> {
            if (screenNames.contains(screenName)) {
                connection.send(new ServiceScreenLogPacket(screenName, line));
            }
        });
    }

    public CompletableFuture<List<String>> requestLogs(Screen screen) {
        return clusterManager.request(screen.remoteNode(), new RequestServiceLogsPacket(screen.name()), ServiceLogsResponsePacket.class)
                .thenApply(ServiceLogsResponsePacket::lines);
    }

    private void subscribe(NetworkConnection connection, String screenName) {
        if (!isServiceScreen(screenName)) {
            return;
        }

        subscriptions.computeIfAbsent(connection, ignored -> ConcurrentHashMap.newKeySet()).add(screenName);
        screenOpened(screens.get(screenName));
    }

    private void unsubscribe(NetworkConnection connection, String screenName) {
        final Set<String> connectionSubscriptions = subscriptions.get(connection);
        if (connectionSubscriptions != null) {
            connectionSubscriptions.remove(screenName);
        }
    }

    private void unsubscribeAll(NetworkConnection connection) {
        subscriptions.remove(connection);
    }

    private void sendLogs(PacketContext<RequestServiceLogsPacket> ctx) {
        final String name = ctx.packet().serviceName();
        if (!isServiceScreen(name)) {
            ctx.reply(new ServiceLogsResponsePacket(List.of()));
            return;
        }

        screens.logs(name)
                .exceptionally(_ -> List.of())
                .thenAccept(lines -> ctx.reply(new ServiceLogsResponsePacket(lines)));
    }

    private boolean isServiceScreen(String name) {
        final Screen screen = screens.get(name);
        return screen != null && screen.type() == ScreenType.SERVICE;
    }
}
