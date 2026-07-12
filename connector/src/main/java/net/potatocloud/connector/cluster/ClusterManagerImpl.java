package net.potatocloud.connector.cluster;

import net.potatocloud.api.cluster.ClusterManager;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packets.cluster.ClusterNodeAddPacket;
import net.potatocloud.network.packets.cluster.ClusterNodeRemovePacket;
import net.potatocloud.network.packets.cluster.ClusterNodesResponsePacket;
import net.potatocloud.network.packets.cluster.RequestClusterNodesPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterManagerImpl implements ClusterManager {

    private volatile ClusterNode localNode;
    private final Map<String, ClusterNode> nodes = new ConcurrentHashMap<>();

    public ClusterManagerImpl(NetworkClient client) {
        client.on(ClusterNodeAddPacket.class, ctx -> nodes.put(ctx.packet().node().name(), ctx.packet().node()));

        client.on(ClusterNodeRemovePacket.class, ctx -> nodes.remove(ctx.packet().nodeName()));

        client.request(new RequestClusterNodesPacket(), ClusterNodesResponsePacket.class)
                .thenAccept(response -> {
                    localNode = response.localNode();
                    response.remoteNodes().forEach(node -> nodes.put(node.name(), node));
                });
    }

    @Override
    public ClusterNode localNode() {
        return localNode;
    }

    @Override
    public List<ClusterNode> nodes() {
        final List<ClusterNode> all = new ArrayList<>(nodes.values());
        all.add(localNode);
        return Collections.unmodifiableList(all);
    }

    @Override
    public Optional<ClusterNode> find(String name) {
        if (localNode != null && localNode.name().equals(name)) {
            return Optional.of(localNode);
        }
        return Optional.ofNullable(nodes.get(name));
    }
}
