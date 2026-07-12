package net.potatocloud.node.service.handlers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.StopServicePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import java.util.Optional;

public final class StopServiceHandler implements PacketHandler<StopServicePacket> {

    private final ServiceManager serviceManager;
    private final ClusterManagerImpl clusterManager;

    public StopServiceHandler(ServiceManager serviceManager, ClusterManagerImpl clusterManager) {
        this.serviceManager = serviceManager;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<StopServicePacket> ctx) {
        serviceManager.find(ctx.packet().serviceName()).ifPresent(service -> {
            final Optional<ClusterNode> node = service.node();

            if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
                clusterManager.sendTo(node.get().name(), ctx.packet());
                return;
            }

            serviceManager.stop(service);
        });
    }
}
