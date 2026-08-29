package net.potatocloud.node.service;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.common.FileUtils;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.service.*;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.impl.LocalServiceScreen;
import net.potatocloud.node.screen.impl.NodeScreen;
import net.potatocloud.node.service.helper.ServiceIds;
import net.potatocloud.node.service.helper.ServicePorts;
import net.potatocloud.node.service.runtime.JvmServiceRuntime;
import net.potatocloud.node.service.runtime.ServiceAliveChecker;
import net.potatocloud.node.service.runtime.ServiceMemoryUpdater;
import net.potatocloud.node.service.runtime.ServiceRuntime;
import net.potatocloud.node.template.TemplateManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public final class NodeServiceManager implements ServiceManager {

    private final Map<String, Service> services = new ConcurrentHashMap<>();
    private final Map<String, ServiceRuntime> runtimes = new ConcurrentHashMap<>();

    private final NetworkServer server;
    private final Logger logger;
    private final NodeConfig config;
    private final EventBus eventBus;
    private final GroupManagerImpl groupManager;
    private final ScreenManager screenManager;
    private final TemplateManager templateManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;
    private final ClusterManagerImpl clusterManager;
    private final Console console;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

    public NodeServiceManager(
            NodeConfig config,
            Logger logger,
            NetworkServer server,
            EventBus eventBus,
            GroupManagerImpl groupManager,
            ScreenManager screenManager,
            TemplateManager templateManager,
            DownloadManager downloadManager,
            CacheManager cacheManager,
            ClusterManagerImpl clusterManager,
            Console console
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
        this.console = console;

        ServiceDefaultFiles.copyDefaultFiles(Path.of(config.folders().data()));

        ServicePacketHandlers.register(
                server,
                this,
                groupManager,
                eventBus,
                clusterManager,
                logger,
                screenManager
        );

        scheduler.scheduleAtFixedRate(
                new ServiceAliveChecker(runtimes, this),
                0,
                1,
                TimeUnit.SECONDS
        );


        scheduler.scheduleAtFixedRate(
                new ServiceMemoryUpdater(runtimes, this, server, clusterManager),
                0,
                2,
                TimeUnit.SECONDS
        );
    }

    @Override
    public Optional<Service> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(services.get(name.toLowerCase()));
    }

    @Override
    public List<Service> services() {
        return services.values().stream().toList();
    }

    @Override
    public CompletableFuture<Service> start(Group group) {
        if (group == null) {
            return CompletableFuture.completedFuture(null);
        }

        final Optional<ClusterNode> node = group.node();
        if (node.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (!clusterManager.isLocal(node.get().name())) {
            return clusterManager.request(
                    node.get().name(),
                    new StartServicePacket(group.name()),
                    StartServiceResponsePacket.class
            ).thenApply(StartServiceResponsePacket::service);
        }

        return CompletableFuture.completedFuture(startService(group.name()));
    }

    @Override
    public CompletableFuture<Void> stop(Service service) {
        final Optional<ClusterNode> node = service.node();

        if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
            clusterManager.sendTo(node.get().name(), new StopServicePacket(service.name()));
            return CompletableFuture.completedFuture(null);
        }

        return stopService(service);
    }

    @Override
    public void copyTo(Service service, String template, String filter) {
        final Optional<ClusterNode> node = service.node();

        if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
            clusterManager.sendTo(node.get().name(), new ServiceCopyPacket(service.name(),  template, filter));
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
        final Optional<ClusterNode> node = service.node();

        if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
            clusterManager.sendTo(node.get().name(), new ServiceExecuteCommandPacket(service.name(), command));
            return;
        }

        final ServiceRuntime runtime = runtimes.get(service.name());
        if (runtime != null) {
            runtime.executeCommand(command);
        }
    }

    @Override
    public void update(Service service) {
        final ServiceUpdatePacket packet = new ServiceUpdatePacket(
                service.name(),
                service.state().name(),
                service.maxPlayers(),
                service.properties()
        );
        server.broadcast().connectors().send(packet);
        clusterManager.broadcast(packet);
    }

    private Service startService(String groupName) {
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
                new HashMap<>(group.get().properties()),
                Instant.ofEpochSecond(0L),
                ServiceState.STOPPED,
                group.get().maxPlayers(),
                0
        );

        final Screen screen = new LocalServiceScreen(service, console);
        screenManager.register(screen);

        final JvmServiceRuntime runtime = new JvmServiceRuntime(
                group.get(), config, logger, templateManager, downloadManager, cacheManager, screen
        );

        addService(service);
        runtimes.put(name, runtime);

        server.broadcast().connectors().send(new ServiceAddPacket(service));
        clusterManager.broadcast(new ServiceAddPacket(service));

        service.startedAt(Instant.now());
        service.state(ServiceState.PREPARING);

        executor.execute(() -> {
            try {
                if (service.state() == ServiceState.STOPPING || service.state() == ServiceState.STOPPED) {
                    return;
                }

                runtime.prepare(service);

                if (service.state() == ServiceState.STOPPING || service.state() == ServiceState.STOPPED) {
                    runtime.stop(service);
                    return;
                }

                service.state(ServiceState.STARTING);
                update(service);
                runtime.start(service);

                final String nodeInfo = config.cluster().enabled() ? " on node &a" + clusterManager.localNode().name() + "&7" : "";
                logger.info("Service &a" + service.name() + "&7 is starting" + nodeInfo
                        + " &8[&7Port&8: &a" + service.port()
                        + "&8, &7Group&8: &a" + groupName + "&8]");

                clusterManager.broadcast(new ServiceStartingPacket(service.name()));
                eventBus.publish(new ServiceStartingEvent(service.name()));
            } catch (Exception e) {
                logger.error("Failed to start service &a" + service.name() + "&8: &c" + e.getMessage());
                runtimes.remove(service.name());
                services.remove(service.name().toLowerCase());
                screenManager.unregister(service.name());
                server.broadcast().connectors().send(new ServiceRemovePacket(service.name(), service.host(), service.port()));
                clusterManager.broadcast(new ServiceRemovePacket(service.name(), service.host(), service.port()));
            }
        });

        return service;
    }

    private CompletableFuture<Void> stopService(Service service) {
        synchronized (service) {
            if (service.state() == ServiceState.STOPPED || service.state() == ServiceState.STOPPING) {
                return CompletableFuture.completedFuture(null);
            }
            service.state(ServiceState.STOPPING);
        }

        logger.info("Service &a" + service.name() + "&7 is now stopping&8...");
        eventBus.publish(new ServiceStoppingEvent(service.name()));

        return CompletableFuture.runAsync(() -> {
            final ServiceRuntime runtime = runtimes.remove(service.name());
            if (runtime != null) {
                runtime.stop(service);
            }

            services.remove(service.name().toLowerCase());

            if (screenManager.current() != null && screenManager.current().name().equals(service.name())) {
                screenManager.open(screenManager.get(NodeScreen.NODE_SCREEN_NAME));
            }

            screenManager.unregister(service.name());

        server.broadcast().connectors().send(new ServiceRemovePacket(service.name(), service.host(), service.port()));
        clusterManager.broadcast(new ServiceRemovePacket(service.name(), service.host(), service.port()));
            eventBus.publish(new ServiceStoppedEvent(service.name()));

            synchronized (service) {
                service.state(ServiceState.STOPPED);
            }

            logger.info("Service &a" + service.name() + " &7has been stopped");
        }, executor);
    }

    public void addService(Service service) {
        services.put(service.name().toLowerCase(), service);
    }

    public void removeService(Service service) {
        services.remove(service.name().toLowerCase());
        screenManager.unregister(service.name());
    }

    @Override
    public Optional<Service> current() {
        throw new UnsupportedOperationException("current() is only available when the API is used from within a connector.");
    }
}
