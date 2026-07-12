package net.potatocloud.node.service.handlers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceStartingPacket;

import java.util.Optional;

public final class ServiceStartingHandler implements PacketHandler<ServiceStartingPacket> {

    private final Logger logger;
    private final ServiceManager serviceManager;

    public ServiceStartingHandler(Logger logger, ServiceManager serviceManager) {
        this.logger = logger;
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceStartingPacket> ctx) {
        serviceManager.find(ctx.packet().serviceName()).ifPresent(service -> {
            service.state(ServiceState.STARTING);

            final Optional<ClusterNode> node = service.node();
            if (node.isEmpty()) {
                return;
            }

            logger.info("Service &a" + service.name() + "&7 is starting on node &a" + node.get().name()
                    + " &8[&7Port&8: &a" + service.port()
                    + "&8, &7Group&8: &a" + service.group().name() + "&8]");
        });
    }
}
