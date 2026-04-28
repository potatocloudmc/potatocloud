package net.potatocloud.node.service;

import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.packets.service.*;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformManagerImpl;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.listeners.*;
import net.potatocloud.node.template.TemplateManager;
import net.potatocloud.node.utils.NetworkUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ServiceManagerImpl implements ServiceManager {

    private final List<Service> services = new CopyOnWriteArrayList<>();

    private final NodeConfig config;
    private final Logger logger;
    private final NetworkServer server;
    private final EventManager eventManager;
    private final ServiceGroupManager groupManager;
    private final ScreenManager screenManager;
    private final TemplateManager templateManager;
    private final PlatformManagerImpl platformManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;
    private final Console console;

    public ServiceManagerImpl(
            NodeConfig config,
            Logger logger,
            NetworkServer server,
            EventManager eventManager,
            ServiceGroupManager groupManager,
            ScreenManager screenManager,
            TemplateManager templateManager,
            PlatformManagerImpl platformManager,
            DownloadManager downloadManager,
            CacheManager cacheManager,
            Console console
    ) {
        this.config = config;
        this.logger = logger;
        this.server = server;
        this.eventManager = eventManager;
        this.groupManager = groupManager;
        this.screenManager = screenManager;
        this.templateManager = templateManager;
        this.platformManager = platformManager;
        this.downloadManager = downloadManager;
        this.cacheManager = cacheManager;
        this.console = console;

        server.on(RequestServicesPacket.class, new RequestServicesListener(this));
        server.on(ServiceStartedPacket.class, new ServiceStartedListener(this, logger, eventManager));
        server.on(ServiceUpdatePacket.class, new ServiceUpdateListener(this, server));
        server.on(StartServicePacket.class, new StartServiceListener(this, groupManager));
        server.on(StopServicePacket.class, new StopServiceListener(this));
        server.on(ServiceExecuteCommandPacket.class, new ServiceExecuteCommandListener(this));
        server.on(ServiceCopyPacket.class, new ServiceCopyListener(this));
    }

    @Override
    public Service getService(String name) {
        return services.stream()
                .filter(service -> service.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Service> getAllServices() {
        return Collections.unmodifiableList(services);
    }

    @Override
    public void updateService(Service service) {
        server.generateBroadcast().broadcast(new ServiceUpdatePacket(
                service.getName(),
                service.getStatus().name(),
                service.getMaxPlayers(),
                service.getPropertyMap()
        ));
    }

    public Service startServiceInternal(String groupName, String requestId) {
        final ServiceGroup group = groupManager.getServiceGroup(groupName);
        if (group == null) {
            return null;
        }

        final int serviceId = getFreeServiceId(group);
        final int port = getServicePort(group);

        final ServiceImpl service = new ServiceImpl(
                serviceId,
                port,
                group,
                config,
                logger,
                server,
                screenManager,
                templateManager,
                platformManager,
                downloadManager,
                cacheManager,
                eventManager,
                this,
                console
        );

        services.add(service);

        server.generateBroadcast().broadcast(new ServiceAddPacket(
                service.getName(),
                service.getServiceId(),
                service.getServiceUuid(),
                service.getPort(),
                service.getStartTimestamp(),
                service.getGroup().getName(),
                service.getPropertyMap(),
                service.getStatus().name(),
                service.getMaxPlayers(),
                requestId
        ));

        service.start();

        return service;
    }

    @Override
    public void startService(String groupName) {
        startServiceInternal(groupName, null);
    }

    @Override
    public CompletableFuture<Service> startServiceAsync(String groupName) {
        final Service service = startServiceInternal(groupName, null);
        return CompletableFuture.completedFuture(service);
    }

    public void removeService(Service service) {
        services.remove(service);
    }

    private int getFreeServiceId(ServiceGroup group) {
        final Set<Integer> usedIds = services.stream()
                .filter(service -> service.getServiceGroup().equals(group))
                .map(Service::getServiceId)
                .collect(Collectors.toSet());

        int id = 1;
        while (usedIds.contains(id)) {
            id++;
        }
        return id;
    }

    private int getServicePort(ServiceGroup group) {
        int port = group.getPlatform().isProxy() ? config.getProxyStartPort() : config.getServiceStartPort();

        final Set<Integer> usedPorts = services.stream()
                .map(Service::getPort)
                .collect(Collectors.toSet());

        while (usedPorts.contains(port) || !NetworkUtils.isPortFree(port)) {
            port++;
        }

        return port;
    }

    public boolean hasEnoughMemory(ServiceGroup group) {
        if (!config.isMemoryCheckEnabled()) {
            return true;
        }

        final long usedMb = services.stream()
                .mapToLong(service -> service.getServiceGroup().getMaxMemory())
                .sum();

        return (usedMb + group.getMaxMemory()) <= config.getMaxMemory();
    }

    public void logMemoryWarning(ServiceGroup group) {
        final long usedMb = services.stream()
                .mapToLong(service -> service.getServiceGroup().getMaxMemory())
                .sum();

        logger.warn("Service(s) for group &a" + group.getName()
                + " &7could not be started &8[&7Required&8: &a" + group.getMaxMemory() + " MB"
                + "&8, &7Used&8: &a" + usedMb + " MB"
                + "&8, &7Max&8: &a" + config.getMaxMemory() + " MB&8]");
    }

    @Override
    public Service getCurrentService() {
        return null;
    }
}
