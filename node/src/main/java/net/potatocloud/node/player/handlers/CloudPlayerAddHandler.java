package net.potatocloud.node.player.handlers;

import lombok.RequiredArgsConstructor;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.player.CloudPlayerAddPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.player.CloudPlayerManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerAddHandler implements PacketHandler<CloudPlayerAddPacket> {

    private final CloudPlayerManagerImpl playerManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    @Override
    public void handle(PacketContext<CloudPlayerAddPacket> ctx) {
        final CloudPlayerAddPacket packet = ctx.packet();

        playerManager.registerPlayer(packet.player());

        final Node node = Node.instance();

        server.broadcast().connectors().exclude(ctx.connection()).send(packet);

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }

        final NodeConfig config = node.config();
        if (config.console().logPlayerConnections()) {
            node.logger().info("Player &a" + packet.player().username() + " &7connected to the network &8[&7UUID&8: &a"
                    + packet.player().uniqueId() + "&8, &7Proxy&8: &a" + packet.player().proxy().name() + "&8]");
        }
    }
}
