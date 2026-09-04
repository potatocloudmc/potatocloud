package net.potatocloud.node.service.handler;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceStartedPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;

import java.util.Optional;

public final class ServiceStartedHandler implements PacketHandler<ServiceStartedPacket> {

    private final ServiceManager serviceManager;
    private final Logger logger;
    private final EventBus eventBus;
    private final ClusterManagerImpl clusterManager;
    private final NetworkServer server;

    public ServiceStartedHandler(
            ServiceManager serviceManager,
            Logger logger,
            EventBus eventBus,
            ClusterManagerImpl clusterManager,
            NetworkServer server
    ) {
        this.serviceManager = serviceManager;
        this.logger = logger;
        this.eventBus = eventBus;
        this.clusterManager = clusterManager;
        this.server = server;
    }

    @Override
    public void handle(PacketContext<ServiceStartedPacket> ctx) {
        final ServiceStartedPacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service -> {
            if (ctx.connection().type() == ConnectionType.NODE && service.state() == ServiceState.RUNNING) {
                server.broadcast().connectors().send(packet);
                return;
            }

            if (service.state() != ServiceState.STARTING) {
                return;
            }

            final Optional<ClusterNode> node = service.node();
            if (node.isEmpty()) {
                return;
            }

            final boolean clustered = Node.instance().config().cluster().enabled();
            logger.info("Service &a" + packet.serviceName() + (clustered ? "&7 is now &aonline &7on node &a" + node.get().name() : "&7 is now &aonline"));

            final long startupTime = System.currentTimeMillis() - service.startedAt().toEpochMilli();
            logger.debug("Service &a" + packet.serviceName() + "&7 took &a" + TimeFormatter.formatAsDuration(startupTime) + "&7 to start");

            service.state(ServiceState.RUNNING);
            serviceManager.update(service);

            if (ctx.connection().type() == ConnectionType.CONNECTOR) {
                clusterManager.broadcast(new ServiceStartedPacket(packet.serviceName()));
            }

            // needed for velocity plugin to register servers on start
            server.broadcast().connectors().send(new ServiceStartedPacket(packet.serviceName()));

            eventBus.publish(new ServiceStartedEvent(packet.serviceName()));
        });
    }
}
