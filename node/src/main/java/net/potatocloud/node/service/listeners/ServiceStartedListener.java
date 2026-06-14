package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.ServiceStartedPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.service.NodeService;
import net.potatocloud.node.service.runtime.ServiceMemoryUpdateTask;
import net.potatocloud.node.service.runtime.ServiceProcessChecker;

import java.util.Optional;

@RequiredArgsConstructor
public class ServiceStartedListener implements PacketListener<ServiceStartedPacket> {

    private final ServiceManager serviceManager;
    private final Logger logger;
    private final EventBus eventBus;
    private final ClusterManagerImpl clusterManager;
    private final NetworkServer server;

    @Override
    public void handle(PacketContext<ServiceStartedPacket> ctx) {
        final ServiceStartedPacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service -> {
            final Optional<ClusterNode> node = service.node();
            if (node.isEmpty()) {
                return;
            }

            final boolean clustered = Node.getInstance().config().cluster().enabled();
            logger.info("Service &a" + packet.serviceName() + (clustered ? "&7 started on node &a" + node.get().name() : "&7 started"));

            logger.debug("Service &a" + packet.serviceName() + "&7 took &a" + TimeFormatter.formatAsDuration(System.currentTimeMillis() - service.startedAt().toEpochMilli()) + "&7 to start");

            service.state(ServiceState.RUNNING);
            serviceManager.update(service);

            if (ctx.connection().type() == ConnectionType.CONNECTOR) {
                clusterManager.broadcast(new ServiceStartedPacket(packet.serviceName()));
            }

            server.broadcast().connectors().send(new ServiceStartedPacket(packet.serviceName()));

            eventBus.publish(new ServiceStartedEvent(packet.serviceName()));

            if (clusterManager.isLocal(node.get().name())) {
                if (service instanceof NodeService nodeService) {
                    nodeService.setProcessChecker(new ServiceProcessChecker(nodeService));
                }
                new ServiceMemoryUpdateTask(service, server, clusterManager);
            }
        });
    }
}
