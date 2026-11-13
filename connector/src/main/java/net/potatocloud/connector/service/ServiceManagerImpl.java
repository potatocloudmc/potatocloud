package net.potatocloud.connector.service;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.LogListener;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.api.template.Template;
import net.potatocloud.api.template.TemplateCopyOptions;
import net.potatocloud.connector.service.handlers.ServiceAddHandler;
import net.potatocloud.connector.service.handlers.ServiceUpdateHandler;
import net.potatocloud.network.packets.service.*;
import net.potatocloud.network.NetworkClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceManagerImpl implements ServiceManager {

    private final List<Service> services = new CopyOnWriteArrayList<>();
    private final Map<String, Set<LogListener>> logListeners = new ConcurrentHashMap<>();

    private final NetworkClient client;

    public ServiceManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(ServiceAddPacket.class, new ServiceAddHandler(this));
        client.on(ServiceRemovePacket.class, ctx -> {
            find(ctx.packet().serviceName()).ifPresent(services::remove);
            logListeners.remove(ctx.packet().serviceName());
        });
        client.on(ServiceUpdatePacket.class, new ServiceUpdateHandler(this));
        client.on(ServiceMemoryUpdatePacket.class, ctx -> find(ctx.packet().serviceName()).ifPresent(service -> {
            if (service instanceof ServiceImpl serviceImpl) {
                serviceImpl.usedMemory(ctx.packet().usedMemory());
            }
        }));

        client.on(ServiceScreenLogPacket.class, ctx -> notifyLogListeners(ctx.packet().serviceName(), ctx.packet().line()));

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
                service.properties())
        );
    }

    @Override
    public CompletableFuture<Service> start(Group group) {
        return client.request(
                new StartServicePacket(group.name()),
                StartServiceResponsePacket.class
        ).thenApply(StartServiceResponsePacket::service);
    }

    @Override
    public CompletableFuture<Void> stop(Service service) {
        return client.request(new StopServicePacket(service.name()), StopServiceResponsePacket.class).thenApply(response -> null);
    }

    @Override
    public void execute(Service service, String command) {
        client.send(new ServiceExecuteCommandPacket(service.name(), command));
    }

    @Override
    public CompletableFuture<List<String>> logs(Service service) {
        return client.request(new RequestServiceLogsPacket(service.name()), ServiceLogsResponsePacket.class)
                .thenApply(ServiceLogsResponsePacket::lines);
    }

    @Override
    public void addLogListener(Service service, LogListener listener) {
        final Set<LogListener> listeners = logListeners.computeIfAbsent(service.name(), name -> ConcurrentHashMap.newKeySet());
        if (listeners.isEmpty()) {
            client.send(new ServiceScreenSubscribePacket(service.name()));
        }
        listeners.add(listener);
    }

    @Override
    public void removeLogListener(Service service, LogListener listener) {
        final Set<LogListener> listeners = logListeners.get(service.name());
        if (listeners == null || !listeners.remove(listener)) {
            return;
        }

        if (listeners.isEmpty()) {
            client.send(new ServiceScreenUnsubscribePacket(service.name()));
        }
    }

    private void notifyLogListeners(String serviceName, String line) {
        for (LogListener listener : logListeners.getOrDefault(serviceName, Set.of())) {
            listener.onLog(line);
        }
    }

    @Override
    public void copyTo(Service service, Template template, TemplateCopyOptions options) {
        client.send(new ServiceCopyPacket(service.name(), template, options));
    }

    @Override
    public Optional<Service> current() {
        return find(System.getProperty("potatocloud.service.name"));
    }
}