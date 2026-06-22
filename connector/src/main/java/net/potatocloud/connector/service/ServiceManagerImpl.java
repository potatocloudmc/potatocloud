package net.potatocloud.connector.service;

import lombok.Getter;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.connector.service.listeners.ServiceAddListener;
import net.potatocloud.connector.service.listeners.ServiceUpdateListener;
import net.potatocloud.network.packet.packets.service.ServicesResponsePacket;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packet.packets.service.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceManagerImpl implements ServiceManager {

    private final List<Service> services = new CopyOnWriteArrayList<>();

    @Getter
    private final Map<String, CompletableFuture<Service>> pendingStarts = new ConcurrentHashMap<>();

    private final NetworkClient client;

    public ServiceManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(ServiceAddPacket.class, new ServiceAddListener(this));
        client.on(ServiceRemovePacket.class, ctx -> find(ctx.packet().serviceName()).ifPresent(services::remove));
        client.on(ServiceUpdatePacket.class, new ServiceUpdateListener(this));
        client.on(ServiceMemoryUpdatePacket.class, ctx -> find(ctx.packet().serviceName()).ifPresent(service -> {
            if (service instanceof ServiceImpl serviceImpl) {
                serviceImpl.usedMemory(ctx.packet().usedMemory());
            }
        }));

        client.request(new RequestServicesPacket(), ServicesResponsePacket.class).thenAccept(response -> response.services().forEach(this::addService));
    }

    public void addService(Service service) {
        services.add(service);
    }

    @Override
    public Optional<Service> find(String name) {
        return services.stream().filter(service -> service.name().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public List<Service> services() {
        return Collections.unmodifiableList(services);
    }

    @Override
    public void update(Service service) {
        client.send(new ServiceUpdatePacket(
                service.name(),
                service.state().name(),
                service.maxPlayers(),
                service.propertyMap())
        );
    }

    @Override
    public CompletableFuture<Service> start(Group group) {
        final CompletableFuture<Service> future = new CompletableFuture<>();
        final String requestId = UUID.randomUUID().toString();

        pendingStarts.put(requestId, future);
        client.send(new StartServicePacket(group.name(), requestId));

        return future;
    }

    @Override
    public CompletableFuture<Void> stop(Service service) {
        // TODO: Use response packets for shutdown instead of fire-and-forget.
        client.send(new StopServicePacket(service.name()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void execute(Service service, String command) {
        client.send(new ServiceExecuteCommandPacket(service.name(), command));
    }

    @Override
    public void copyTo(Service service, String template, String filter) {
        client.send(new ServiceCopyPacket(service.name(), template, filter));
    }

    @Override
    public Optional<Service> current() {
        return find(System.getProperty("potatocloud.service.name"));
    }
}