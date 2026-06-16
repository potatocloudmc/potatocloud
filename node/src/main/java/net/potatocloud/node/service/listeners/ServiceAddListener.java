package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.ServiceAddPacket;
import net.potatocloud.node.service.ServiceManagerImpl;

public class ServiceAddListener implements PacketListener<ServiceAddPacket> {

    private final ServiceManagerImpl serviceManager;
    private final NetworkServer server;

    public ServiceAddListener(ServiceManagerImpl serviceManager, NetworkServer server) {
        this.serviceManager = serviceManager;
        this.server = server;
    }

    @Override
    public void handle(PacketContext<ServiceAddPacket> ctx) {
        final ServiceAddPacket packet = ctx.packet();

        if (serviceManager.find(packet.service().name()).isPresent()) {
            return;
        }

        serviceManager.addService(packet.service());
        server.broadcast().connectors().send(new ServiceAddPacket(packet.service(), null));
    }
}
