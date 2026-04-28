package net.potatocloud.node;

import com.google.gson.Gson;
import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.translation.TranslationManager;
import net.potatocloud.common.FileUtils;
import net.potatocloud.core.event.ServerEventManager;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.netty.server.NettyNetworkServer;
import net.potatocloud.core.networking.packet.PacketManager;
import net.potatocloud.core.networking.packet.packets.logging.LogMessagePacket;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.command.commands.*;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.database.MySQLHandler;
import net.potatocloud.node.group.ServiceGroupManagerImpl;
import net.potatocloud.node.logging.NodeLogger;
import net.potatocloud.node.module.ModuleLoader;
import net.potatocloud.node.module.ModuleManager;
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
import net.potatocloud.node.translation.TranslationManagerImpl;
import net.potatocloud.node.utils.HardwareUtils;
import net.potatocloud.node.utils.NetworkUtils;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Getter
public class Node extends CloudAPI {

    private final long startupTime;
    private final NodeConfig config;

    private final NodeLogger logger;
    private final Console console;
    private final ScreenManager screenManager;
    private final CommandManager commandManager;

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

    private final TranslationManager translationManager;
    private final MySQLHandler mySQLHandler = new MySQLHandler();

    private final ModuleManager moduleManager;
    private final ModuleLoader moduleLoader;

    private final Gson gson = new Gson();
    private boolean ready = false;
    private boolean stopping;

    public Node(long startupTime) {
        this.startupTime = startupTime;

        config = new NodeConfig();
        config.load();

        config.reload();

        if (!NetworkUtils.isPortFree(config.getNodePort())) {
            System.err.println("The configured node port is already in use. Is another instance of potatocloud already running on this port?");
            System.exit(0);
        }

        commandManager = new CommandManager();
        console = new Console(config, commandManager);
        logger = new NodeLogger(config, console, Path.of(config.getLogsFolder()));

        commandManager.setLogger(logger);

        final Screen nodeScreen = new Screen(Screen.NODE_SCREEN);
        screenManager = new ScreenManager(console, logger);
        screenManager.register(nodeScreen);
        screenManager.setCurrentScreen(nodeScreen);

        console.start();

        if (HardwareUtils.isLowHardware()) {
            logger.warn("Your hardware is low, you may experience performance issues. Recommended: 4 cores, 4GB RAM");
        }

        setupManager = new SetupManager();

        packetManager = new PacketManager();
        server = new NettyNetworkServer(packetManager);
        server.start(config.getNodeHost(), config.getNodePort());
        logger.info("Network server started using &aNetty &7on &a" + config.getNodeHost() + "&8:&a" + config.getNodePort());

        // TODO: Maybe move this somewhere else
        // Handle logs from Connector
        server.on(LogMessagePacket.class, (_, packet) -> logger.log(Logger.Level.valueOf(packet.getLevel()), packet.getMessage()));

        translationManager = new TranslationManagerImpl(server);
        eventManager = new ServerEventManager(server);
        propertiesHolder = new NodePropertiesHolder(server);
        playerManager = new CloudPlayerManagerImpl(server);
        templateManager = new TemplateManager(logger, Path.of(config.getTemplatesFolder()));
        groupManager = new ServiceGroupManagerImpl(Path.of(config.getGroupsFolder()), server, logger);

        if (!groupManager.getAllServiceGroups().isEmpty()) {
            final int count = groupManager.getAllServiceGroups().size();

            logger.info("Loaded &a" + count + "&7 " + (count == 1 ? "group" : "groups") + "&8:");

            groupManager.getAllServiceGroups().forEach(group -> logger.info("&8» &a" + group.getName()));
        }

        platformManager = new PlatformManagerImpl(logger, server);

        if (!platformManager.getPlatforms().isEmpty()) {
            logger.info("Loaded &a" + platformManager.getPlatforms().size() + "&7 platforms&8:");

            platformManager.getPlatforms().forEach(platform -> logger.info("&8» &a" + platform.getName()));
        }

        downloadManager = new DownloadManager(Path.of(config.getPlatformsFolder()), logger);
        cacheManager = new CacheManager(logger);

        ServiceDefaultFiles.copyDefaultFiles(Path.of(config.getDataFolder()));
        serviceManager = new ServiceManagerImpl(
                config, logger, server, eventManager, groupManager, screenManager, templateManager, platformManager, downloadManager, cacheManager, console
        );
        serviceStartScheduler = new ServiceStartScheduler(config, groupManager, serviceManager, eventManager);

        moduleManager = new ModuleManager();
        moduleLoader = new ModuleLoader(moduleManager);
        moduleLoader.load(Path.of(config.getModulesFolder()));

        if (!moduleManager.getModules().isEmpty()) {
            final int count = moduleManager.getModules().size();

            logger.info("Loaded &a" + count + "&7 module" + (count == 1 ? "" : "s") + "&8:");

            moduleManager.getModules().values().forEach(module -> logger.info("&8» &a" + module.getName() + " &7v" + module.getVersion()));
        }

        moduleManager.enableAll();

        registerCommands();

        logger.info("Startup completed in &a" + (System.currentTimeMillis() - startupTime) + "ms &8| &7Use &8'&ahelp&8' &7to see available commands");

        serviceStartScheduler.start();
        ready = true;

        if (!groupManager.existsServiceGroup("debug")) {
            groupManager.createServiceGroup("debug", "paper", "1.21.8", 1, 3,50,2048,true, true, 100, 10);
        }
        if (!groupManager.existsServiceGroup("velocity")) {
            groupManager.createServiceGroup("velocity", "velocity", "latest", 1, 2,1000,1024, false, true, 100, 10);
        }
    }

    public static Node getInstance() {
        return (Node) CloudAPI.getInstance();
    }

    private void registerCommands() {
        commandManager.registerCommand(new ClearCommand(console));
        commandManager.registerCommand(new GroupCommand(logger, groupManager));
        commandManager.registerCommand(new HelpCommand(logger, commandManager));
        commandManager.registerCommand(new InfoCommand(logger));
        commandManager.registerCommand(new PlatformCommand(logger, platformManager));
        commandManager.registerCommand(new PlayerCommand(logger, playerManager));
        commandManager.registerCommand(new ServiceCommand(logger, serviceManager, screenManager));
    }

    public void shutdown() {
        if (stopping) {
            return;
        }

        logger.info("Shutting down node&8...");
        stopping = true;

        serviceStartScheduler.close();

        moduleManager.disableAll();

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

        logger.info("Disconnecting from database server&8...");
        mySQLHandler.close();

        logger.info("Cleaning up temporary files&8...");
        FileUtils.deleteDirectory(Path.of(config.getTempServicesFolder()));

        logger.info("Shutdown complete. Goodbye!");

        //console.close();
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
    public TranslationManager getTranslationManager() {
        return null;
    }

    @Override
    public PropertyHolder getGlobalProperties() {
        return propertiesHolder;
    }
}
