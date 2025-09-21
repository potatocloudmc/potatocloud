package net.potatocloud.plugin.api.impl.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.service.ServiceAddPacket;
import net.potatocloud.plugin.api.impl.service.ServiceImpl;
import net.potatocloud.plugin.api.impl.service.ServiceManagerImpl;

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
                packet.getProperties(),
                ServiceStatus.valueOf(packet.getStatus()),
                packet.getMaxPlayers()
        );

        if (!serviceManager.getAllServices().contains(service)) {
            serviceManager.addService(service);
        }
    }
}
