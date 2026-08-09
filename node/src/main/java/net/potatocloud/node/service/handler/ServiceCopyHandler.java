package net.potatocloud.node.service.handler;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceCopyPacket;

public final class ServiceCopyHandler implements PacketHandler<ServiceCopyPacket> {

    private final ServiceManager serviceManager;

    public ServiceCopyHandler(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceCopyPacket> ctx) {
        final ServiceCopyPacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service ->
                serviceManager.copyTo(service, packet.templateName(), packet.filter()));
    }
}
