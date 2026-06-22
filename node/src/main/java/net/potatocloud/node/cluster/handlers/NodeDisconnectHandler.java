package net.potatocloud.node.cluster.handlers;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.node.cluster.ClusterManagerImpl;

import java.util.function.Consumer;

public class NodeDisconnectHandler implements Consumer<NetworkConnection> {

    private final ClusterManagerImpl clusterManager;
    private final Logger logger;

    public NodeDisconnectHandler(ClusterManagerImpl clusterManager, Logger logger) {
        this.clusterManager = clusterManager;
        this.logger = logger;
    }

    @Override
    public void accept(NetworkConnection connection) {
        // only warn if no clean leave packet was received before disconnect
        clusterManager.remoteNode(connection).ifPresent(node -> {
            clusterManager.remove(node);
            logger.warn("Cluster node &a" + node.name() + " &7lost connection");
        });
    }
}
