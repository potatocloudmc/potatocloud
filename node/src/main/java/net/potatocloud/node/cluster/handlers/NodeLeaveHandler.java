package net.potatocloud.node.cluster.handlers;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.cluster.NodeLeavePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

public final class NodeLeaveHandler implements PacketHandler<NodeLeavePacket> {

    private final ClusterManagerImpl clusterManager;
    private final Logger logger;

    public NodeLeaveHandler(ClusterManagerImpl clusterManager, Logger logger) {
        this.clusterManager = clusterManager;
        this.logger = logger;
    }

    @Override
    public void handle(PacketContext<NodeLeavePacket> ctx) {
        clusterManager.remoteNode(ctx.packet().nodeName()).ifPresent(node -> {
            clusterManager.remove(node);
            logger.info("Cluster node &a" + node.name() + " &7left the cluster");
        });
    }
}
