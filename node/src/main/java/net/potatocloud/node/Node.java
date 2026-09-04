package net.potatocloud.node;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterManager;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.common.FileUtils;
import net.potatocloud.eventbus.ServerEventBus;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.security.SecurityConfig;
import net.potatocloud.network.netty.NettyNetworkServer;
import net.potatocloud.network.packets.logging.LogMessagePacket;
import net.potatocloud.node.cluster.ClusterEventBus;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.command.commands.*;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.config.NodeConfigLoader;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.logging.NodeLogger;
import net.potatocloud.node.migration.MigrationManager;
import net.potatocloud.node.module.ModuleManager;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformManagerImpl;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.properties.NodePropertiesHolder;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.NodeServiceManager;
import net.potatocloud.node.service.start.ServiceStartScheduler;
import net.potatocloud.node.setup.SetupManager;
import net.potatocloud.node.template.TemplateManager;
import net.potatocloud.node.utils.NetworkUtils;
import net.potatocloud.node.utils.SystemUtils;
import net.potatocloud.node.version.UpdateChecker;
import net.potatocloud.node.version.VersionFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Node extends CloudAPI {

    private final long startupTime;
    private final NodeConfigLoader configLoader;
    private final MigrationManager migrationManager;
    private final NodeConfig config;

    private final CommandManager commandManager;
    private final Console console;
    private final NodeLogger logger;
    private final ScreenManager screenManager;
    private final SetupManager setupManager;
    private final UpdateChecker updateChecker;

    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;
    private final ClusterEventBus eventBus;

    private final NodePropertiesHolder propertiesHolder;
    private final CloudPlayerManagerImpl playerManager;
    private final TemplateManager templateManager;
    private final GroupManagerImpl groupManager;

    private final PlatformManagerImpl platformManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;

    private final ModuleManager moduleManager;

    private final NodeServiceManager serviceManager;
    private final ServiceStartScheduler serviceStartScheduler;

    private boolean ready;
    private boolean stopping;

    public Node(long startupTime) {
        this.startupTime = startupTime;
        this.configLoader = new NodeConfigLoader();

        this.migrationManager = new MigrationManager(VersionFile.read());
        configLoader.load();
        migrationManager.migrate();
        VersionFile.write(CloudAPI.VERSION);
        this.config = configLoader.reload();

        this.commandManager = new CommandManager();
        this.console = new Console(config, commandManager);
        this.screenManager = new ScreenManager(console);
        this.logger = new NodeLogger(config, console, screenManager, Path.of(config.folders().logs()));
        this.commandManager.setLogger(logger);
        this.setupManager = new SetupManager();
        this.updateChecker = new UpdateChecker(logger);

        final SecurityConfig networkSecurity = config.security().toNetworkConfig();

        this.server = new NettyNetworkServer(networkSecurity);

        this.clusterManager = new ClusterManagerImpl(config.node().host(), config.node().port(), config.cluster(), networkSecurity, server, logger);

        this.eventBus = new ClusterEventBus(new ServerEventBus(server), clusterManager);

        this.propertiesHolder = new NodePropertiesHolder(server, clusterManager);
        this.playerManager = new CloudPlayerManagerImpl(server, this.clusterManager);

        this.templateManager = new TemplateManager(logger, Path.of(config.folders().templates()));
        this.groupManager = new GroupManagerImpl(Path.of(config.folders().groups()), server, logger, this.clusterManager);
        this.platformManager = new PlatformManagerImpl(logger, server);
        this.downloadManager = new DownloadManager(Path.of(config.folders().platforms()), logger);
        this.cacheManager = new CacheManager(logger);

        this.moduleManager = new ModuleManager(logger);

        this.serviceManager = new NodeServiceManager(
                config, logger, server, eventBus, groupManager, screenManager, templateManager, downloadManager, cacheManager, this.clusterManager
        );

        this.serviceStartScheduler = new ServiceStartScheduler(config, groupManager, serviceManager, eventBus);
    }

    void start() {
        if (!NetworkUtils.isPortFree(config.node().port())) {
            System.err.println("The configured node port is already in use. Is another instance of potatocloud already running on this port?");
            System.exit(0);
        }

        final Screen nodeScreen = Screen.node(console.defaultPrompt());
        screenManager.register(nodeScreen);
        screenManager.current(nodeScreen);
        screenManager.init(server, clusterManager);

        console.start();

        if (SystemUtils.lowHardware()) {
            logger.warn("Your hardware is low, you may experience performance issues. Recommended: 4 cores, 4GB RAM");
        }

        if (!config.disableUpdateChecker()) {
            updateChecker.checkForUpdates();
        }

        final String host = config.node().host();
        final int port = config.node().port();

        server.start(host, port);
        logger.info("Network server started using &aNetty &7on &a" + host + "&8:&a" + port);

        server.on(LogMessagePacket.class, ctx -> logger.log(Logger.Level.valueOf(ctx.packet().level()), ctx.packet().message()));

        if (config.cluster().enabled()) {
            clusterManager.start(
                    groupManager,
                    serviceManager,
                    playerManager,
                    propertiesHolder,
                    screenManager,
                    eventBus
            );
        }

        final List<Group> groups = groupManager.groups();

        if (!groups.isEmpty()) {
            final int count = groups.size();
            final String groupText = count == 1 ? "group" : "groups";

            logger.info("Loaded &a" + count + "&7 " + groupText + "&8:");
            groups.forEach(group -> logger.info("&8» &a" + group.name()));
        }

        final List<Platform> platforms = platformManager.platforms();

        if (!platforms.isEmpty()) {
            logger.info("Loaded &a" + platforms.size() + "&7 platforms&8:");
            platforms.forEach(platform -> logger.info("&8» &a" + platform.name()));
        }

        moduleManager.load(Path.of(config.folders().modules()));
        moduleManager.enableAll();

        registerCommands();

        logger.info("Startup completed in &a" + (System.currentTimeMillis() - startupTime) + "ms &8| &7Use &8'&ahelp&8' &7to see available commands");

        serviceStartScheduler.start();
        ready = true;
    }

    private void registerCommands() {
        commandManager.registerCommand(new ClearCommand(console));
        commandManager.registerCommand(new GroupCommand(logger, groupManager));
        commandManager.registerCommand(new HelpCommand(logger, commandManager));
        commandManager.registerCommand(new InfoCommand(logger));
        commandManager.registerCommand(new PlatformCommand(logger, platformManager));
        commandManager.registerCommand(new PlayerCommand(logger, playerManager));
        commandManager.registerCommand(new ServiceCommand(logger, serviceManager, screenManager));
        commandManager.registerCommand(new ShutdownCommand(this));

        if (config.cluster().enabled()) {
            commandManager.registerCommand(new ClusterCommand(logger, clusterManager));
        }
    }

    public void shutdown() {
        if (stopping) {
            return;
        }

        logger.info("Shutting down node&8...");
        stopping = true;

        serviceStartScheduler.close();

        moduleManager.disableAll();

        if (config.cluster().enabled()) {
            clusterManager.close();
        }

        stopServices();

        serviceManager.close();
        cacheManager.close();

        logger.info("Stopping network server&8...");
        server.close();

        logger.info("Cleaning up temporary files&8...");
        FileUtils.deleteDirectory(Path.of(config.folders().tempServices()));

        logger.info("Shutdown complete. Goodbye!");
        console.close();
    }

    private void stopServices() {
        final List<Service> servicesToStop = new ArrayList<>();
        final String localNodeName = config.cluster().name();

        for (Service service : serviceManager.services()) {
            if (service.state() == ServiceState.STOPPING || service.state() == ServiceState.STOPPED) {
                continue;
            }

            if (config.cluster().enabled()) {
                final Group group = service.group();

                if (group == null || group.node().isEmpty()) {
                    continue;
                }

                if (!group.node().get().name().equals(localNodeName)) {
                    continue;
                }
            }

            servicesToStop.add(service);
        }

        if (servicesToStop.isEmpty()) {
            return;
        }

        logger.info("Shutting down all running services...");

        CompletableFuture.allOf(
                servicesToStop.stream()
                        .map(serviceManager::stop)
                        .toArray(CompletableFuture[]::new))
                .join();
    }

    public static Node instance() {
        return (Node) CloudAPI.instance();
    }

    public boolean ready() {
        return ready;
    }

    public boolean stopping() {
        return stopping;
    }

    public long uptime() {
        return System.currentTimeMillis() - startupTime;
    }

    @Override
    public NodeLogger logger() {
        return logger;
    }

    @Override
    public GroupManager groupManager() {
        return groupManager;
    }

    @Override
    public ServiceManager serviceManager() {
        return serviceManager;
    }

    @Override
    public PlatformManager platformManager() {
        return platformManager;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }

    @Override
    public CloudPlayerManager playerManager() {
        return playerManager;
    }

    public TemplateManager templateManager() {
        return templateManager;
    }

    public NodeConfig config() {
        return config;
    }

    public ScreenManager screenManager() {
        return screenManager;
    }

    public Console console() {
        return console;
    }

    public SetupManager setupManager() {
        return setupManager;
    }

    public long startupTime() {
        return startupTime;
    }

    public DownloadManager downloadManager() {
        return downloadManager;
    }

    public NetworkServer server() {
        return server;
    }

    @Override
    public PropertyHolder globalProperties() {
        return propertiesHolder;
    }

    @Override
    public ClusterManager clusterManager() {
        return clusterManager;
    }
}
