package net.potatocloud.connector.service.handlers;

import net.potatocloud.api.service.Service;
import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceAddPacket;

import java.util.concurrent.CompletableFuture;

public final class ServiceAddHandler implements PacketHandler<ServiceAddPacket> {

    private final ServiceManagerImpl serviceManager;

    public ServiceAddHandler(ServiceManagerImpl serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceAddPacket> ctx) {
        final ServiceAddPacket packet = ctx.packet();
        final Service service = packet.service();

        if (serviceManager.find(service.name()).isEmpty()) {
            serviceManager.addService(service);
        }

        final String requestId = packet.requestId();
        if (requestId == null) {
            return;
        }

        final CompletableFuture<Service> future = serviceManager.pendingStarts().remove(requestId);
        if (future != null) {
            future.complete(service);
        }
    }
}
