package net.potatocloud.node.service.listeners;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.ServiceMemoryUpdatePacket;

public class ServiceMemoryUpdateListener implements PacketListener<ServiceMemoryUpdatePacket> {

    private final ServiceManager serviceManager;
    private final NetworkServer server;

    public ServiceMemoryUpdateListener(ServiceManager serviceManager, NetworkServer server) {
        this.serviceManager = serviceManager;
        this.server = server;
    }

    @Override
    public void handle(PacketContext<ServiceMemoryUpdatePacket> ctx) {
        if (ctx.connection().type() != ConnectionType.NODE) {
            return;
        }

        serviceManager.find(ctx.packet().serviceName()).ifPresent(service -> {
            if (service instanceof ServiceImpl serviceImpl) {
                serviceImpl.usedMemory(ctx.packet().usedMemory());
            }
        });

        server.broadcast().connectors().send(ctx.packet());
    }
}
