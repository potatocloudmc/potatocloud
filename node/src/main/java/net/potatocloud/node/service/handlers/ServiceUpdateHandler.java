package net.potatocloud.node.service.handlers;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

public final class ServiceUpdateHandler implements PacketHandler<ServiceUpdatePacket> {

    private final ServiceManager serviceManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public ServiceUpdateHandler(ServiceManager serviceManager, NetworkServer server, ClusterManagerImpl clusterManager) {
        this.serviceManager = serviceManager;
        this.server = server;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<ServiceUpdatePacket> ctx) {
        final ServiceUpdatePacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service -> {
            service.state(ServiceState.valueOf(packet.state()));
            service.maxPlayers(packet.maxPlayers());
            service.properties().clear();
            packet.propertyMap().forEach((key, value) -> PropertyUtil.setUnchecked(service, key, value));
        });

        server.broadcast().connectors().exclude(ctx.connection()).send(packet);

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }
    }
}
