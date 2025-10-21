package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.service.RequestServicesPacket;
import net.potatocloud.core.networking.packets.service.ServiceAddPacket;

@RequiredArgsConstructor
public class RequestServicesListener implements PacketListener<RequestServicesPacket> {

    private final ServiceManager serviceManager;

    @Override
    public void onPacket(NetworkConnection connection, RequestServicesPacket packet) {
        for (Service service : serviceManager.getAllServices()) {
            connection.send(new ServiceAddPacket(
                    service.getName(),
                    service.getServiceId(),
                    service.getPort(),
                    service.getStartTimestamp(),
                    service.getServiceGroup().getName(),
                    service.getPropertyMap(),
                    service.getStatus().name(),
                    service.getMaxPlayers()
            ));
        }
    }
}
