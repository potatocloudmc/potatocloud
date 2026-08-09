package net.potatocloud.node.service.handler;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceMemoryUpdatePacket;

public final class ServiceMemoryUpdateHandler implements PacketHandler<ServiceMemoryUpdatePacket> {

    private final ServiceManager serviceManager;
    private final NetworkServer server;

    public ServiceMemoryUpdateHandler(ServiceManager serviceManager, NetworkServer server) {
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
