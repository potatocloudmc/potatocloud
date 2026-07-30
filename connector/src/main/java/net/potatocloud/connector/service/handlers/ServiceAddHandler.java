package net.potatocloud.connector.service.handlers;

import net.potatocloud.api.service.Service;
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
        final Service service = ctx.packet().service();

        if (serviceManager.find(service.name()).isEmpty()) {
            serviceManager.addService(service);
        }
    }
}
