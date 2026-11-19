package net.potatocloud.connector.service.handlers;

import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceAddPacket;

public final class ServiceAddHandler implements PacketHandler<ServiceAddPacket> {

    private final ServiceManagerImpl serviceManager;

    public ServiceAddHandler(ServiceManagerImpl serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceAddPacket> ctx) {
        serviceManager.addService(ctx.packet().service());
    }
}
