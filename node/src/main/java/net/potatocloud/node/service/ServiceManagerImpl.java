package net.potatocloud.node.service;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.service.impl.ServiceImpl;

import net.potatocloud.common.FileUtils;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.packets.service.*;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.helper.ServiceIds;
import net.potatocloud.node.service.helper.ServicePorts;
import net.potatocloud.node.service.listeners.*;
import net.potatocloud.node.service.runtime.ServiceProcessMonitor;
import net.potatocloud.node.service.runtime.ServiceMemoryMonitor;
import net.potatocloud.node.service.runtime.ServiceRuntime;
import net.potatocloud.node.service.runtime.local.LocalJvmRuntime;
import net.potatocloud.node.service.runtime.local.ServiceDefaultFiles;
import net.potatocloud.node.template.TemplateManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ServiceManagerImpl implements ServiceManager {

    private final List<Service> services = new CopyOnWriteArrayList<>();
    private final Map<String, ServiceRuntime> runtimes = new ConcurrentHashMap<>();

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

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

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

        scheduler.scheduleAtFixedRate(new ServiceProcessMonitor(runtimes, this), 0, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new ServiceMemoryMonitor(runtimes, this, server, clusterManager), 0, 2, TimeUnit.SECONDS);
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
        // todo use optionals correct
        if (!clusterManager.isLocal(service.node().get().name())) {
            clusterManager.sendTo(service.node().get().name(), new StopServicePacket(service.name()));
            return CompletableFuture.completedFuture(null);
        }

        return stopService(service);
    }

    @Override
    public void copyTo(Service service, String template, String filter) {
        // todo optionals
        if (!clusterManager.isLocal(service.node().get().name())) {
            clusterManager.sendTo(service.node().get().name(), new ServiceCopyPacket(service.name(), template, filter));
            return;
        }

        final ServiceRuntime runtime = runtimes.get(service.name());
        if (runtime == null) {
            return;
        }

        runtime.directory().ifPresent(serviceDir -> {
            final Path templatesDirectory = Path.of(config.folders().templates());
            Path sourcePath = serviceDir;
            Path targetPath = templatesDirectory.resolve(template);

            if (filter != null && filter.startsWith("/")) {
                sourcePath = serviceDir.resolve(filter.substring(1));
                targetPath = targetPath.resolve(filter.substring(1));
            }

            if (!Files.exists(sourcePath)) {
                return;
            }

            if (!Files.exists(targetPath)) {
                templateManager.createTemplate(targetPath.getFileName().toString());
            }

            FileUtils.copyDirectory(sourcePath, targetPath);
        });
    }

    @Override
    public void execute(Service service, String command) {
        if (!clusterManager.isLocal(service.node().get().name())) {
            clusterManager.sendTo(service.node().get().name(), new ServiceExecuteCommandPacket(service.name(), command));
            return;
        }

        final ServiceRuntime runtime = runtimes.get(service.name());
        if (runtime != null) {
            runtime.executeCommand(command);
        }
    }

    public Service startService(String groupName, String requestId) {
        final Optional<Group> group = groupManager.find(groupName);
        if (group.isEmpty()) {
            return null;
        }

        final int serviceId = ServiceIds.nextId(group.get(), services);
        final int port = ServicePorts.nextPort(group.get(), config, services);
        final String name = group.get().name() + config.service().splitter() + serviceId;

        final ServiceImpl service = new ServiceImpl(
                serviceId,
                clusterManager.localNode().host(),
                port,
                name,
                group.get().name(),
                new HashMap<>(group.get().propertyMap()),
                Instant.ofEpochSecond(0L),
                ServiceState.STOPPED,
                group.get().maxPlayers(),
                0
        );

        final LocalJvmRuntime runtime = new LocalJvmRuntime(
                group.get(), config, logger, templateManager, downloadManager, cacheManager
        );

        final Screen screen = new Screen(name);
        screenManager.register(screen);

        addService(service);
        runtimes.put(name, runtime);

        server.broadcast().connectors().send(new ServiceAddPacket(service, requestId));
        clusterManager.broadcast(new ServiceAddPacket(service, null));

        service.startedAt(Instant.now());

        service.state(ServiceState.PREPARING);
        runtime.prepare(service);

        service.state(ServiceState.STARTING);
        runtime.start(service, screen::addLog);

        final String nodeInfo = config.cluster().enabled() ? " on Node &a" + clusterManager.localNode().name() + "&7" : "";
        logger.info("Service &a" + service.name() + "&7 is starting" + nodeInfo
                + " &8[&7Port&8: &a" + service.port()
                + "&8, &7Group&8: &a" + groupName + "&8]");

        clusterManager.broadcast(new ServiceStartingPacket(service.name()));
        eventBus.publish(new ServiceStartingEvent(service.name()));
        return service;
    }

    private CompletableFuture<Void> stopService(Service service) {
        if (service.state() == ServiceState.STOPPED || service.state() == ServiceState.STOPPING) {
            return CompletableFuture.completedFuture(null);
        }

        service.state(ServiceState.STOPPING);
        logger.info("Service &a" + service.name() + "&7 is now stopping&8...");
        eventBus.publish(new ServiceStoppingEvent(service.name()));

        return CompletableFuture.runAsync(() -> {
            final ServiceRuntime runtime = runtimes.remove(service.name());
            if (runtime != null) {
                runtime.stop(service);
            }

            services.remove(service);
            screenManager.unregister(service.name());

            if (screenManager.getCurrentScreen().name().equals(service.name())) {
                screenManager.switchTo(Screen.NODE_SCREEN);
            }

            server.broadcast().connectors().send(new ServiceRemovePacket(service.name(), service.port()));
            clusterManager.broadcast(new ServiceRemovePacket(service.name(), service.port()));
            eventBus.publish(new ServiceStoppedEvent(service.name()));

            synchronized (service) {
                service.state(ServiceState.STOPPED);
            }

            logger.info("Service &a" + service.name() + " &7has been stopped");
        }, executor);
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
