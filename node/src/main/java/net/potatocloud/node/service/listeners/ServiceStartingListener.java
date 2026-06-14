package net.potatocloud.node.service.listeners;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.ServiceStartingPacket;

import java.util.Optional;

public class ServiceStartingListener implements PacketListener<ServiceStartingPacket> {

    private final Logger logger;
    private final ServiceManager serviceManager;

    public ServiceStartingListener(Logger logger, ServiceManager serviceManager) {
        this.logger = logger;
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceStartingPacket> ctx) {
        serviceManager.find(ctx.packet().serviceName()).ifPresent(service -> {
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
