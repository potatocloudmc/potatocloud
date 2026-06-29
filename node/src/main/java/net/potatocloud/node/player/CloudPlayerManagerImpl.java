package net.potatocloud.node.player;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.player.*;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.player.handlers.CloudPlayerAddHandler;
import net.potatocloud.node.player.handlers.CloudPlayerRemoveHandler;
import net.potatocloud.node.player.handlers.CloudPlayerUpdateHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CloudPlayerManagerImpl implements CloudPlayerManager {

    private final Set<CloudPlayer> onlinePlayers = new HashSet<>();
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public CloudPlayerManagerImpl(NetworkServer server, ClusterManagerImpl clusterManager) {
        this.server = server;
        this.clusterManager = clusterManager;

        server.on(CloudPlayerAddPacket.class, new CloudPlayerAddHandler(this, server, clusterManager));
        server.on(CloudPlayerRemovePacket.class, new CloudPlayerRemoveHandler(this, clusterManager));
        server.on(CloudPlayerUpdatePacket.class, new CloudPlayerUpdateHandler(this, server, clusterManager));
        server.on(RequestCloudPlayersPacket.class, ctx -> ctx.reply(new CloudPlayersResponsePacket(players().stream().toList())));
        server.on(CloudPlayerConnectPacket.class, ctx -> server.broadcast().connectors().send(ctx.packet()));
    }

    public void registerPlayer(CloudPlayer player) {
        onlinePlayers.add(player);
    }

    public void unregisterPlayer(CloudPlayer player) {
        onlinePlayers.remove(player);
    }

    @Override
    public Optional<CloudPlayer> find(UUID uniqueId) {
        return onlinePlayers.stream()
                .filter(player -> player.uniqueId().equals(uniqueId))
                .findFirst();
    }

    @Override
    public Optional<CloudPlayer> find(String username) {
        return onlinePlayers.stream()
                .filter(player -> player.username().equals(username))
                .findFirst();
    }

    @Override
    public Set<CloudPlayer> players() {
        return Collections.unmodifiableSet(onlinePlayers);
    }

    @Override
    public void connectTo(CloudPlayer player, Service service) {
        final CloudPlayerConnectPacket packet = new CloudPlayerConnectPacket(player.username(), service.name());

        if (player.proxy().node().isPresent()) {
            final String nodeName = player.proxy().node().get().name();

            if (!clusterManager.isLocal(nodeName)) {
                clusterManager.sendTo(nodeName, packet);
                return;
            }
        }

        server.broadcast().connectors().send(packet);
    }

    @Override
    public void update(CloudPlayer player) {}

}

