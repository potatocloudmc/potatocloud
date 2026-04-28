package net.potatocloud.connector.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.connector.service.ServiceImpl;
import net.potatocloud.connector.service.ServiceManagerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.service.ServiceAddPacket;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ServiceAddListener implements PacketListener<ServiceAddPacket> {

    private final ServiceManagerImpl serviceManager;

    @Override
    public void onPacket(NetworkConnection connection, ServiceAddPacket packet) {
        final Service service = new ServiceImpl(
                packet.getName(),
                packet.getServiceId(),
                packet.getServiceUuid(),
                packet.getPort(),
                packet.getStartTimestamp(),
                CloudAPI.getInstance().getServiceGroupManager().getServiceGroup(packet.getGroupName()),
                packet.getPropertyMap(),
                ServiceStatus.valueOf(packet.getStatus()),
                packet.getMaxPlayers(),
                0
        );

        final List<Service> services = serviceManager.getAllServices();
        if (!services.contains(service)) {
            serviceManager.addService(service);
        }

        final String requestId = packet.getRequestId();
        if (requestId == null) {
            return;
        }

        final CompletableFuture<Service> future = serviceManager.getPendingStarts().remove(requestId);
        if (future != null) {
            future.complete(service);
        }
    }
}
