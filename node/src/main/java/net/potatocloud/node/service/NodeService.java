package net.potatocloud.node.service;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.common.FileUtils;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.packets.service.ServiceRemovePacket;
import net.potatocloud.network.packet.packets.service.ServiceStartingPacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.runtime.ServiceProcessChecker;
import net.potatocloud.node.service.runtime.ServiceRuntime;
import net.potatocloud.node.template.TemplateManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NodeService extends ServiceImpl {

    private final Group group;
    private final Path directory;
    private final NodeConfig config;
    private final Logger logger;
    private final TemplateManager templateManager;

    private final NetworkServer server;
    private final EventBus eventBus;
    private final ServiceManager serviceManager;
    private final ScreenManager screenManager;
    private final Console console;
    private final ClusterManagerImpl clusterManager;
    private final Screen screen;
    private final List<String> logs = new ArrayList<>();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final ServiceRuntime runtime;

    private ServiceProcessChecker processChecker;

    public NodeService(
            int serviceId,
            int port,
            Group group,
            NodeConfig config,
            Logger logger,
            NetworkServer server,
            EventBus eventBus,
            ServiceManager serviceManager,
            TemplateManager templateManager,
            ScreenManager screenManager,
            Console console,
            ServiceRuntime runtime,
            ClusterManagerImpl clusterManager
    ) {
        super(serviceId, clusterManager.localNode().host(), port, group.name() + config.service().splitter() + serviceId, group.name(), new HashMap<>(group.propertyMap()), Instant.ofEpochSecond(0L), ServiceState.STOPPED, group.maxPlayers(), 0);
        this.group = group;
        this.config = config;
        this.logger = logger;
        this.server = server;
        this.eventBus = eventBus;
        this.serviceManager = serviceManager;
        this.templateManager = templateManager;
        this.screenManager = screenManager;
        this.console = console;
        this.runtime = runtime;
        this.clusterManager = clusterManager;
        this.screen = new Screen(name());
        this.directory = resolveDirectory();
    }

    public void start() {
        if (running()) {
            return;
        }

        startedAt(Instant.now());
        screenManager.register(screen);

        state(ServiceState.PREPARING);
        runtime.prepare(this);

        state(ServiceState.STARTING);
        runtime.start(this);

        // todo
        final String nodeInfo = config.cluster().enabled() ? " on Node &a" + node().get().name() + "&7" : "";
        logger.info("Service &a" + name() + "&7 is starting" + nodeInfo
                + " &8[&7Port&8: &a" + port()
                + "&8, &7Group&8: &a" + group.name() + "&8]"
        );

        clusterManager.broadcast(new ServiceStartingPacket(name()));

        eventBus.publish(new ServiceStartingEvent(name()));
    }

    public CompletableFuture<Void> shutdown() {
        if (state() == ServiceState.STOPPED || state() == ServiceState.STOPPING) {
            return CompletableFuture.completedFuture(null);
        }

        state(ServiceState.STOPPING);
        logger.info("Service &a" + name() + "&7 is now stopping&8...");
        eventBus.publish(new ServiceStoppingEvent(name()));

        return CompletableFuture.runAsync(() -> {
            if (processChecker != null) {
                processChecker.close();
            }

            runtime.stop(this);

            ((ServiceManagerImpl) serviceManager).removeService(this);
            screenManager.unregister(screen.name());

            if (screenManager.getCurrentScreen().name().equals(name())) {
                screenManager.switchTo(Screen.NODE_SCREEN);
            }

            server.broadcast().connectors().send(new ServiceRemovePacket(name(), port()));
            clusterManager.broadcast(new ServiceRemovePacket(name(), port()));
            eventBus.publish(new ServiceStoppedEvent(name()));

            if (!group.staticServices() && Files.exists(directory)) {
                FileUtils.deleteDirectory(directory);
            }

            synchronized (this) {
                state(ServiceState.STOPPED);
            }

            logger.info("Service &a" + name() + " &7has been stopped");
        }, executorService);
    }

    public void copy(String template, String filter) {
        final Path templatesDirectory = Path.of(config.folders().templates());

        Path sourcePath = directory;
        Path targetPath = templatesDirectory.resolve(template);

        if (filter != null && filter.startsWith("/")) {
            sourcePath = directory.resolve(filter.substring(1));
            targetPath = targetPath.resolve(filter.substring(1));
        }

        if (!Files.exists(sourcePath)) {
            return;
        }

        if (!Files.exists(targetPath)) {
            templateManager.createTemplate(targetPath.getFileName().toString());
        }

        FileUtils.copyDirectory(sourcePath, targetPath);
    }

    public void executeCommand(String command) {
        runtime.executeCommand(command);
    }

    public boolean alive() {
        return runtime.isAlive();
    }

    @Override
    public int usedMemory() {
        return runtime.usedMemory();
    }

    public Logger getLogger() {
        return logger;
    }

    public Screen getScreen() {
        return screen;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setProcessChecker(ServiceProcessChecker processChecker) {
        this.processChecker = processChecker;
    }

    public void log(String log) {
        logs.add(log);
        screen.addLog(log);
        // TODO Remove console
        if (screenManager.getCurrentScreen().name().equals(name())) {
            console.println(log);
        }
    }

    private Path resolveDirectory() {
        if (group.staticServices()) {
            return Path.of(config.folders().staticServices()).resolve(name());
        }
        return Path.of(config.folders().tempServices()).resolve(name() + "-" + UUID.randomUUID());
    }
}
