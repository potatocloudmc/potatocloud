package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.service.ServiceUpdatePacket;
import net.potatocloud.common.PropertyUtil;

@RequiredArgsConstructor
public class ServiceUpdateListener implements PacketListener<ServiceUpdatePacket> {

    private final ServiceManager serviceManager;
    private final NetworkServer server;

    @Override
    public void onPacket(NetworkConnection connection, ServiceUpdatePacket packet) {
        final Service service = serviceManager.getService(packet.getServiceName());
        if (service == null) {
            return;
        }

        service.setStatus(ServiceStatus.valueOf(packet.getStatus()));
        service.setMaxPlayers(packet.getMaxPlayers());
        service.getPropertyMap().clear();
        for (Property<?> property : packet.getPropertyMap().values()) {
            PropertyUtil.setPropertyUnchecked(service, property);
        }

        server.generateBroadcast().exclude(connection).broadcast(packet);
    }
}
