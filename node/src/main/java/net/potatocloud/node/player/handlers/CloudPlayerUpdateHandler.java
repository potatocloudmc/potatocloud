package net.potatocloud.node.player.handlers;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerUpdateHandler implements PacketHandler<CloudPlayerUpdatePacket> {

    private final CloudPlayerManager playerManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    @Override
    public void handle(PacketContext<CloudPlayerUpdatePacket> ctx) {
        final CloudPlayerUpdatePacket packet = ctx.packet();

        playerManager.find(packet.playerUniqueId()).ifPresent(player -> {
            if (player instanceof CloudPlayerImpl playerImpl) {
                playerImpl.proxyName(packet.connectedProxyName());
                playerImpl.serviceName(packet.connectedServiceName());

                playerImpl.propertyMap().clear();
                for (Property<?> property : packet.propertyMap().values()) {
                    PropertyUtil.setPropertyUnchecked(playerImpl, property);
                }
            }
        });

        server.broadcast().connectors().exclude(ctx.connection()).send(packet);

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }
    }
}
