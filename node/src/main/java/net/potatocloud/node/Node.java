package net.potatocloud.node;

import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.module.PotatoModule;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.utils.version.Version;
import net.potatocloud.common.FileUtils;
import net.potatocloud.core.event.ServerEventManager;
import net.potatocloud.core.migration.MigrationManager;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.netty.server.NettyNetworkServer;
import net.potatocloud.core.networking.packet.PacketManager;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.command.commands.*;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.group.ServiceGroupManagerImpl;
import net.potatocloud.node.migration.Migration_1_4_3;
import net.potatocloud.node.migration.Migration_1_4_4;
import net.potatocloud.node.module.ModuleLoader;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformManagerImpl;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.properties.NodePropertiesHolder;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.ServiceDefaultFiles;
import net.potatocloud.node.service.ServiceImpl;
import net.potatocloud.node.service.ServiceManagerImpl;
import net.potatocloud.node.service.start.ServiceStartScheduler;
import net.potatocloud.node.setup.SetupManager;
import net.potatocloud.node.template.TemplateManager;
import net.potatocloud.node.utils.HardwareUtils;
import net.potatocloud.node.utils.NetworkUtils;
import net.potatocloud.node.version.UpdateChecker;
import net.potatocloud.node.version.VersionFile;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Getter
public class Node extends CloudAPI {

    private final long startupTime;
    private final NodeConfig config;

    private final Logger logger;
    private final Console console;
    private final ScreenManager screenManager;
    private final CommandManager commandManager;

    private final MigrationManager migrationManager;
    private final PacketManager packetManager;
    private final NetworkServer server;
    private final EventManager eventManager;

    private final NodePropertiesHolder propertiesHolder;
    private final CloudPlayerManager playerManager;
    private final TemplateManager templateManager;
    private final ServiceGroupManager groupManager;

    private final PlatformManagerImpl platformManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;

    private final ServiceManagerImpl serviceManager;
    private final ServiceStartScheduler serviceStartScheduler;

    private final SetupManager setupManager;
    private final UpdateChecker updateChecker;

    private final ModuleLoader moduleLoader;

    private final Version previousVersion;
    private boolean ready = false;
    private boolean stopping;

    public Node(long startupTime) {
        this.startupTime = startupTime;

        config = new NodeConfig();

        previousVersion = VersionFile.read();
        migrationManager = new MigrationManager(previousVersion);
        registerMigrations();
        migrationManager.migrate();

        VersionFile.write(CloudAPI.VERSION);

        config.load();

        if (!NetworkUtils.isPortFree(config.getNodePort())) {
            System.err.println("The configured node port is already in use. Is another instance of potatocloud already running on this port?");
            System.exit(0);
        }

        commandManager = new CommandManager();
        console = new Console(config, commandManager);
        logger = new Logger(config, console, Path.of(config.getLogsFolder()));

        commandManager.setLogger(logger);

        final Screen nodeScreen = new Screen(Screen.NODE_SCREEN);
        screenManager = new ScreenManager(console, logger);
        screenManager.addScreen(nodeScreen);
        screenManager.setCurrentScreen(nodeScreen);

        console.start();

        if (HardwareUtils.isLowHardware()) {
            logger.warn("Your hardware is low, you may experience performance issues. Recommended: 4 cores, 4GB RAM");
        }

        setupManager = new SetupManager();

        updateChecker = new UpdateChecker(logger);

        if (!config.isDisableUpdateChecker()) {
            updateChecker.checkForUpdates();
        }

        packetManager = new PacketManager();
        server = new NettyNetworkServer(packetManager);
        server.start(config.getNodeHost(), config.getNodePort());
        logger.info("Network server started using &aNetty &7on &a" + config.getNodeHost() + "&8:&a" + config.getNodePort());

        eventManager = new ServerEventManager(server);
        propertiesHolder = new NodePropertiesHolder(server);
        playerManager = new CloudPlayerManagerImpl(server);
        templateManager = new TemplateManager(logger, Path.of(config.getTemplatesFolder()));
        groupManager = new ServiceGroupManagerImpl(Path.of(config.getGroupsFolder()), server, logger);

        if (!groupManager.getAllServiceGroups().isEmpty()) {
            final int groupCount = groupManager.getAllServiceGroups().size();
            logger.info("Loaded &a" + groupCount + "&7 " + (groupCount == 1 ? "group" : "groups") + "&8:");

            for (ServiceGroup group : groupManager.getAllServiceGroups()) {
                logger.info("&8» &a" + group.getName());
            }
        }

        platformManager = new PlatformManagerImpl(logger, server);
        downloadManager = new DownloadManager(Path.of(config.getPlatformsFolder()), logger);
        cacheManager = new CacheManager(logger);

        ServiceDefaultFiles.copyDefaultFiles(Path.of(config.getDataFolder()));
        serviceManager = new ServiceManagerImpl(
                config, logger, server, eventManager, groupManager, screenManager, templateManager, platformManager, downloadManager, cacheManager, console
        );
        serviceStartScheduler = new ServiceStartScheduler(config, groupManager, serviceManager, eventManager);

        registerCommands();

        logger.info("Startup completed in &a" + (System.currentTimeMillis() - startupTime) + "ms &8| &7Use &8'&ahelp&8' &7to see available commands");

        moduleLoader = new ModuleLoader(logger);
        moduleLoader.loadModules();

        serviceStartScheduler.start();
        ready = true;
    }

    public static Node getInstance() {
        return (Node) CloudAPI.getInstance();
    }

    private void registerMigrations() {
        new Migration_1_4_3(Path.of(config.getConfig().getString("folders.groups")), migrationManager);
        new Migration_1_4_4(migrationManager);
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
    }

    public void shutdown() {
        if (stopping) {
            return;
        }

        logger.info("Shutting down node&8...");
        stopping = true;

        serviceStartScheduler.close();

        moduleLoader.getModules().values().forEach(PotatoModule::onDisable);

        if (!serviceManager.getAllServices().isEmpty()) {
            logger.info("Shutting down all running services&8...");

            CompletableFuture.allOf(
                    serviceManager.getAllServices().stream()
                            .map(service -> ((ServiceImpl) service).shutdownAsync())
                            .toArray(CompletableFuture[]::new)
            ).join();
        }

        logger.info("Stopping network server&8...");
        server.close();

        logger.info("Cleaning up temporary files&8...");
        FileUtils.deleteDirectory(Path.of(config.getTempServicesFolder()));

        logger.info("Shutdown complete. Goodbye!");

        // console.close();
        System.exit(0);
    }

    public long getUptime() {
        return System.currentTimeMillis() - startupTime;
    }

    @Override
    public ServiceGroupManager getServiceGroupManager() {
        return groupManager;
    }

    @Override
    public PropertyHolder getGlobalProperties() {
        return propertiesHolder;
    }
}
