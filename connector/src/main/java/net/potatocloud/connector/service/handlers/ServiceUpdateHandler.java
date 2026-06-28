package net.potatocloud.connector.service.handlers;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.service.ServiceUpdatePacket;

public final class ServiceUpdateHandler implements PacketHandler<ServiceUpdatePacket> {

    private final ServiceManager serviceManager;

    public ServiceUpdateHandler(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<ServiceUpdatePacket> ctx) {
        final ServiceUpdatePacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service -> {
            service.state(ServiceState.valueOf(packet.state()));
            service.maxPlayers(packet.maxPlayers());

            service.properties().clear();
            packet.propertyMap().forEach((key, value) -> PropertyUtil.setUnchecked(service, key, value));
        });
    }
}
