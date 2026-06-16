package net.potatocloud.node.service.listeners;

import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.ServiceRemovePacket;
import net.potatocloud.node.service.ServiceManagerImpl;

public class ServiceRemoveListener implements PacketListener<ServiceRemovePacket> {

    private final ServiceManagerImpl serviceManager;
    private final NetworkServer server;

    public ServiceRemoveListener(ServiceManagerImpl serviceManager, NetworkServer server) {
        this.serviceManager = serviceManager;
        this.server = server;
    }

    @Override
    public void handle(PacketContext<ServiceRemovePacket> ctx) {
        serviceManager.find(ctx.packet().serviceName()).ifPresent(serviceManager::removeService);
        server.broadcast().connectors().exclude(ctx.connection()).send(ctx.packet());
    }
}
