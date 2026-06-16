package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.ServiceUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

public class ServiceUpdateListener implements PacketListener<ServiceUpdatePacket> {

    private final ServiceManager serviceManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public ServiceUpdateListener(ServiceManager serviceManager, NetworkServer server, ClusterManagerImpl clusterManager) {
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
            service.propertyMap().clear();
            for (Property<?> property : packet.propertyMap().values()) {
                PropertyUtil.setPropertyUnchecked(service, property);
            }
        });

        server.broadcast().connectors().exclude(ctx.connection()).send(packet);

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }
    }
}
