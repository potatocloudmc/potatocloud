package net.potatocloud.node.player.handlers;

import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

public final class CloudPlayerUpdateHandler implements PacketHandler<CloudPlayerUpdatePacket> {

    private final CloudPlayerManager playerManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public CloudPlayerUpdateHandler(CloudPlayerManager playerManager, NetworkServer server, ClusterManagerImpl clusterManager) {
        this.playerManager = playerManager;
        this.server = server;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<CloudPlayerUpdatePacket> ctx) {
        final CloudPlayerUpdatePacket packet = ctx.packet();

        playerManager.find(packet.playerUniqueId()).ifPresent(player -> {
            if (player instanceof CloudPlayerImpl playerImpl) {
                playerImpl.proxyName(packet.connectedProxyName());
                playerImpl.serviceName(packet.connectedServiceName());

                playerImpl.properties().clear();
                packet.propertyMap().forEach((key, value) -> PropertyUtil.setUnchecked(playerImpl, key, value));
            }
        });

        server.broadcast().connectors().exclude(ctx.connection()).send(packet);

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }
    }
}
