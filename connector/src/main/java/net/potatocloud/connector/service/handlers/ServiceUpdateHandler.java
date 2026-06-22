package net.potatocloud.connector.service.handlers;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.service.ServiceUpdatePacket;

@RequiredArgsConstructor
public class ServiceUpdateHandler implements PacketHandler<ServiceUpdatePacket> {

    private final ServiceManager serviceManager;

    @Override
    public void handle(PacketContext<ServiceUpdatePacket> ctx) {
        final ServiceUpdatePacket packet = ctx.packet();

        serviceManager.find(packet.serviceName()).ifPresent(service -> {
            service.state(ServiceState.valueOf(packet.state()));
            service.maxPlayers(packet.maxPlayers());

            service.propertyMap().clear();
            for (Property<?> property : packet.propertyMap().values()) {
                PropertyUtil.setPropertyUnchecked(service, property);
            }
        });
    }
}
