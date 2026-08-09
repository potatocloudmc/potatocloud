package net.potatocloud.node.service.handler;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceExecuteCommandPacket;

public final class ServiceExecuteCommandHandler implements PacketHandler<ServiceExecuteCommandPacket> {

    private final ServiceManager serviceManager;

    public ServiceExecuteCommandHandler(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceExecuteCommandPacket> ctx) {
        final ServiceExecuteCommandPacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service ->
                serviceManager.execute(service, packet.command()));
    }
}
