package net.potatocloud.node.cluster.handlers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.packets.cluster.NodeDiscoveryPacket;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.node.cluster.ClusterManagerImpl;

public final class NodeDiscoveryHandler implements PacketHandler<NodeDiscoveryPacket> {

    private final ClusterManagerImpl clusterManager;

    public NodeDiscoveryHandler(ClusterManagerImpl clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<NodeDiscoveryPacket> ctx) {
        for (ClusterNode node : ctx.packet().nodes()) {
            if (clusterManager.remoteNode(node.name()).isPresent()) {
                continue;
            }
            clusterManager.connect(node.host(), node.port());
        }
    }
}
