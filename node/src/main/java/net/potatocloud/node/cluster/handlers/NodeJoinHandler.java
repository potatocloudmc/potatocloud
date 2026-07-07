package net.potatocloud.node.cluster.handlers;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.packets.cluster.ClusterSyncPacket;
import net.potatocloud.network.packets.cluster.NodeDiscoveryPacket;
import net.potatocloud.network.packets.cluster.NodeJoinPacket;
import net.potatocloud.network.packets.cluster.NodeJoinRejectPacket;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.cluster.ClusterNodeImpl;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.service.ServiceManagerImpl;

public final class NodeJoinHandler implements PacketHandler<NodeJoinPacket> {

    private final ClusterNode localNode;
    private final ClusterManagerImpl clusterManager;
    private final String clusterToken;
    private final Logger logger;
    private final GroupManagerImpl groupManager;
    private final ServiceManagerImpl serviceManager;
    private final CloudPlayerManagerImpl playerManager;
    private final PropertyHolder properties;

    public NodeJoinHandler(ClusterNode localNode, ClusterManagerImpl clusterManager, String clusterToken, Logger logger, GroupManagerImpl groupManager, ServiceManagerImpl serviceManager, CloudPlayerManagerImpl playerManager, PropertyHolder properties) {
        this.localNode = localNode;
        this.clusterManager = clusterManager;
        this.clusterToken = clusterToken;
        this.logger = logger;
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;
        this.playerManager = playerManager;
        this.properties = properties;
    }

    @Override
    public void handle(PacketContext<NodeJoinPacket> ctx) {
        final NodeJoinPacket packet = ctx.packet();
        final NetworkConnection connection = ctx.connection();
        final String nodeName = packet.nodeName();

        if (nodeName.equals(localNode.name())) {
            return;
        }

        if (!clusterToken.equals(packet.clusterToken())) {
            connection.send(new NodeJoinRejectPacket("Wrong cluster token"));
            connection.close();
            return;
        }

        final String localVersion = CloudAPI.VERSION.toString();
        if (!localVersion.equals(packet.nodeVersion())) {
            connection.send(new NodeJoinRejectPacket("Cluster node is running version " + localVersion + " but you are running version " + packet.nodeVersion()));
            connection.close();
            return;
        }

        if (clusterManager.remoteNode(nodeName).isPresent()) {
            connection.send(new NodeJoinRejectPacket("Cluster node '" + nodeName + "' is already connected to this cluster"));
            connection.close();
            return;
        }

        connection.type(ConnectionType.NODE);

        final ClusterNodeImpl node = new ClusterNodeImpl(nodeName, packet.host(), packet.port(), packet.startedAt(), connection);
        clusterManager.add(node);

        if (clusterManager.isOutbound(connection)) {
            logger.info("Connected to cluster node &a" + node.name() + " &8(&a" + node.host() + "&8:&a" + node.port() + "&8)");
        } else {
            logger.info("Cluster node &a" + node.name() + " &7connected to the cluster &8(&a" + node.host() + "&8:&a" + node.port() + "&8)");
            connection.send(new NodeJoinPacket(localNode.name(), localNode.host(), localNode.port(), localNode.startedAt(), CloudAPI.VERSION.toString(), clusterToken));

            connection.send(new NodeDiscoveryPacket(
                    clusterManager.remoteNodes().stream()
                            .filter(n -> !n.name().equals(nodeName))
                            .map(n -> (ClusterNode) n)
                            .toList()
            ));
        }

        final long syncStart = System.currentTimeMillis();

        connection.send(new ClusterSyncPacket(
                groupManager.groups(),
                serviceManager.services(),
                playerManager.players(),
                properties.properties()
        ));

        logger.debug("Cluster sync sent to node &a" + node.name() + " &7in &a" + (System.currentTimeMillis() - syncStart) + "ms");
    }
}
