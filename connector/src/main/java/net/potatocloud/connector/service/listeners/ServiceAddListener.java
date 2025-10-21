package net.potatocloud.connector.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.connector.service.ServiceImpl;
import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.service.ServiceAddPacket;

@RequiredArgsConstructor
public class ServiceAddListener implements PacketListener<ServiceAddPacket> {

    private final ServiceManagerImpl serviceManager;

    @Override
    public void onPacket(NetworkConnection connection, ServiceAddPacket packet) {
        final Service service = new ServiceImpl(
                packet.getName(),
                packet.getServiceId(),
                packet.getPort(),
                packet.getStartTimestamp(),
                CloudAPI.getInstance().getServiceGroupManager().getServiceGroup(packet.getGroupName()),
                packet.getPropertyMap(),
                ServiceStatus.valueOf(packet.getStatus()),
                packet.getMaxPlayers()
        );

        if (!serviceManager.getAllServices().contains(service)) {
            serviceManager.addService(service);
        }
    }
}
