package net.potatocloud.node.service.listeners;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.StopServicePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import java.util.Optional;

public class StopServiceListener implements PacketListener<StopServicePacket> {

    private final ServiceManager serviceManager;
    private final ClusterManagerImpl clusterManager;

    public StopServiceListener(ServiceManager serviceManager, ClusterManagerImpl clusterManager) {
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
