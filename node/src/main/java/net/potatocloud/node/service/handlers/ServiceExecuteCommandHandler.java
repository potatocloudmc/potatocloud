package net.potatocloud.node.service.handlers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.service.ServiceExecuteCommandPacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

import java.util.Optional;

public class ServiceExecuteCommandHandler implements PacketHandler<ServiceExecuteCommandPacket> {

    private final ServiceManager serviceManager;
    private final ClusterManagerImpl clusterManager;

    public ServiceExecuteCommandHandler(ServiceManager serviceManager, ClusterManagerImpl clusterManager) {
        this.serviceManager = serviceManager;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<ServiceExecuteCommandPacket> ctx) {
        final ServiceExecuteCommandPacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service -> {
            final Optional<ClusterNode> node = service.node();

            if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
                clusterManager.sendTo(node.get().name(), packet);
                return;
            }

            serviceManager.execute(service, packet.command());
        });
    }
}
