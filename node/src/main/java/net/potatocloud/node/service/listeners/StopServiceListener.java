package net.potatocloud.node.service.listeners;

import lombok.AllArgsConstructor;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.StopServicePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.service.NodeService;

import java.util.Optional;

@AllArgsConstructor
public class StopServiceListener implements PacketListener<StopServicePacket> {

    private final ServiceManager serviceManager;
    private final ClusterManagerImpl clusterManager;

    @Override
    public void handle(PacketContext<StopServicePacket> ctx) {
        serviceManager.find(ctx.packet().serviceName()).ifPresent(service -> {
            final Optional<ClusterNode> node = service.node();

            if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
                clusterManager.sendTo(node.get().name(), ctx.packet());
                return;
            }

            if (service instanceof NodeService nodeService) {
                nodeService.shutdown();
            }
        });
    }
}
