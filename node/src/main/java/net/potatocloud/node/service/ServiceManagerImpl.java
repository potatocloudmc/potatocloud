package net.potatocloud.node.service;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.packets.service.*;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.helper.ServiceIds;
import net.potatocloud.node.service.helper.ServicePorts;
import net.potatocloud.node.service.listeners.*;
import net.potatocloud.node.service.runtime.local.LocalJvmRuntime;
import net.potatocloud.node.service.runtime.local.ServiceDefaultFiles;
import net.potatocloud.node.template.TemplateManager;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceManagerImpl implements ServiceManager {

    private final List<Service> services = new CopyOnWriteArrayList<>();

    private final NetworkServer server;
    private final Logger logger;
    private final NodeConfig config;
    private final EventBus eventBus;
    private final GroupManager groupManager;
    private final ScreenManager screenManager;
    private final TemplateManager templateManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;
    private final ClusterManagerImpl clusterManager;

    public ServiceManagerImpl(
            NodeConfig config,
            Logger logger,
            NetworkServer server,
            EventBus eventBus,
            GroupManager groupManager,
            ScreenManager screenManager,
            TemplateManager templateManager,
            DownloadManager downloadManager,
            CacheManager cacheManager,
            ClusterManagerImpl clusterManager
    ) {
        this.config = config;
        this.logger = logger;
        this.server = server;
        this.eventBus = eventBus;
        this.groupManager = groupManager;
        this.screenManager = screenManager;
        this.templateManager = templateManager;
        this.downloadManager = downloadManager;
        this.cacheManager = cacheManager;
        this.clusterManager = clusterManager;

        ServiceDefaultFiles.copyDefaultFiles(Path.of(config.folders().data()));

        server.on(RequestServicesPacket.class, new RequestServicesListener(this));
        server.on(ServiceAddPacket.class, new ServiceAddListener(this, server));
        server.on(ServiceRemovePacket.class, new ServiceRemoveListener(this, server));
        server.on(ServiceStartedPacket.class, new ServiceStartedListener(this, logger, eventBus, clusterManager, server));
        server.on(ServiceUpdatePacket.class, new ServiceUpdateListener(this, server, clusterManager));
        server.on(ServiceStartingPacket.class, new ServiceStartingListener(logger, this));
        server.on(StartServicePacket.class, new StartServiceListener(this, groupManager, clusterManager));
        server.on(StopServicePacket.class, new StopServiceListener(this, clusterManager));
        server.on(ServiceExecuteCommandPacket.class, new ServiceExecuteCommandListener(this, clusterManager));
        server.on(ServiceCopyPacket.class, new ServiceCopyListener(this, clusterManager));
        server.on(ServiceMemoryUpdatePacket.class, new ServiceMemoryUpdateListener(this, server));
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
        final ServiceUpdatePacket packet = new ServiceUpdatePacket(
                service.name(),
                service.state().name(),
                service.maxPlayers(),
                service.propertyMap()
        );
        server.broadcast().connectors().send(packet);
        clusterManager.broadcast(packet);
    }

    @Override
    public CompletableFuture<Service> start(Group group) {
        if (group == null) {
            return CompletableFuture.completedFuture(null);
        }

        return group.node()
                .map(node -> {
                    if (!clusterManager.isLocal(node.name())) {
                        clusterManager.sendTo(node.name(), new StartServicePacket(group.name(), null));
                        return null;
                    }

                    return startService(group.name(), null);
                })
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    @Override
    public CompletableFuture<Void> stop(Service service) {
        // todo
        if (!clusterManager.isLocal(service.node().get().name())) {
            clusterManager.sendTo(service.node().get().name(), new StopServicePacket(service.name()));
            return CompletableFuture.completedFuture(null);
        }

        if (!(service instanceof NodeService nodeService)) {
            return CompletableFuture.completedFuture(null);
        }

        return nodeService.shutdown();
    }

    @Override
    public void copyTo(Service service, String template, String filter) {
        if (!clusterManager.isLocal(service.node().get().name())) {
            clusterManager.sendTo(service.node().get().name(), new ServiceCopyPacket(service.name(), template, filter));
            return;
        }

        if (service instanceof NodeService nodeService) {
            nodeService.copy(template, filter);
        }
    }

    @Override
    public void execute(Service service, String command) {
        // todo
        if (!clusterManager.isLocal(service.node().get().name())) {
            clusterManager.sendTo(service.node().get().name(), new ServiceExecuteCommandPacket(service.name(), command));
            return;
        }

        if (service instanceof NodeService nodeService) {
            nodeService.executeCommand(command);
        }
    }

    public Service startService(String groupName, String requestId) {
        final Optional<Group> group = groupManager.find(groupName);
        if (group.isEmpty()) {
            return null;
        }

        final int serviceId = ServiceIds.nextId(group.get(), services);
        final int port = ServicePorts.nextPort(group.get(), config, services);

        final LocalJvmRuntime runtime = new LocalJvmRuntime(
                group.get(), config, logger, templateManager, downloadManager, cacheManager
        );

        final NodeService service = new NodeService(
                serviceId, port, group.get(),
                config, logger, server, eventBus, this, templateManager,
                screenManager, Node.getInstance().console(), runtime, clusterManager // TODO Remove console
        );

        addService(service);

        server.broadcast().connectors().send(new ServiceAddPacket(service, requestId));
        clusterManager.broadcast(new ServiceAddPacket(service, null));

        service.start();
        return service;
    }

    public void addService(Service service) {
        services.add(service);
    }

    public void removeService(Service service) {
        services.remove(service);
    }

    public boolean hasEnoughMemory(Group group) {
        if (!config.service().memoryCheckEnabled()) {
            return true;
        }

        final long usedMb = services.stream()
                .mapToLong(service -> service.group().maxMemory())
                .sum();

        return (usedMb + group.maxMemory()) <= config.service().maxMemory();
    }

    public void logMemoryWarning(Group group) {
        final long usedMb = services.stream()
                .mapToLong(service -> service.group().maxMemory())
                .sum();

        logger.warn("Service(s) for group &a" + group.name()
                + " &7could not be started &8[&7Required&8: &a" + group.maxMemory() + " MB"
                + "&8, &7Used&8: &a" + usedMb + " MB"
                + "&8, &7Max&8: &a" + config.service().maxMemory() + " MB&8]");
    }

    @Override
    public Optional<Service> current() {
        throw new UnsupportedOperationException("getCurrentService() is only available when the API is used from within a connector.");
    }
}
