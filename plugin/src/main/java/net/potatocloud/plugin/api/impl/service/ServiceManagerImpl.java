package net.potatocloud.plugin.api.impl.service;

import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.service.RequestServicesPacket;
import net.potatocloud.core.networking.packets.service.ServiceRemovePacket;
import net.potatocloud.core.networking.packets.service.ServiceUpdatePacket;
import net.potatocloud.core.networking.packets.service.StartServicePacket;
import net.potatocloud.plugin.api.impl.service.listeners.ServiceAddListener;
import net.potatocloud.plugin.api.impl.service.listeners.ServiceUpdateListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceManagerImpl implements ServiceManager {

    private final List<Service> services = new CopyOnWriteArrayList<>();
    private final NetworkClient client;

    public ServiceManagerImpl(NetworkClient client) {
        this.client = client;

        client.send(new RequestServicesPacket());

        client.registerPacketListener(PacketIds.SERVICE_ADD, new ServiceAddListener(this));

        client.registerPacketListener(PacketIds.SERVICE_REMOVE, (NetworkConnection connection, ServiceRemovePacket packet) -> {
            services.remove(getService(packet.getServiceName()));
        });

        client.registerPacketListener(PacketIds.SERVICE_UPDATE, new ServiceUpdateListener(this));
    }

    public void addService(Service service) {
        services.add(service);
    }

    @Override
    public Service getService(String serviceName) {
        return services.stream()
                .filter(service -> service.getName().equalsIgnoreCase(serviceName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Service> getAllServices() {
        return Collections.unmodifiableList(services);
    }

    @Override
    public void updateService(Service service) {
        client.send(new ServiceUpdatePacket(
                service.getName(),
                service.getStatus().name(),
                service.getMaxPlayers(),
                service.getProperties())
        );
    }

    @Override
    public void startService(String groupName) {
        client.send(new StartServicePacket(groupName));
    }

    @Override
    public void startServices(String groupName, int count) {
        for (int i = 0; i < count; i++) {
            startService(groupName);
        }
    }

    @Override
    public Service getCurrentService() {
        return getService(System.getProperty("potatocloud.service.name"));
    }
}