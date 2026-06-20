package net.potatocloud.node.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.player.CloudPlayerManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerRemoveListener implements PacketListener<CloudPlayerRemovePacket> {

    private final CloudPlayerManagerImpl playerManager;
    private final ClusterManagerImpl clusterManager;

    @Override
    public void handle(PacketContext<CloudPlayerRemovePacket> ctx) {
        final CloudPlayerRemovePacket packet = ctx.packet();
        final Node node = Node.instance();

        playerManager.find(packet.playerUniqueId()).ifPresent(player -> {
            playerManager.unregisterPlayer(player);

            if (node.config().console().logPlayerConnections() && !node.stopping()) {
                node.logger().info("Player &a" + player.username() + " &7disconnected &7from the network &8[&7UUID&8: &a" + player.uniqueId() + "&8]");
            }
        });

        node.server().broadcast().connectors().exclude(ctx.connection()).send(packet);

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }
    }
}
