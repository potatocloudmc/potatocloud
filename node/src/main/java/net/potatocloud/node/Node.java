package net.potatocloud.node;

import lombok.Getter;
import lombok.SneakyThrows;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.core.event.ServerEventManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.PacketManager;
import net.potatocloud.core.networking.netty.NettyNetworkServer;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.command.commands.*;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.ExceptionMessageHandler;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.group.ServiceGroupManagerImpl;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformManagerImpl;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.player.CloudPlayerManagerImpl;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.ServiceDefaultFiles;
import net.potatocloud.node.service.ServiceImpl;
import net.potatocloud.node.service.ServiceManagerImpl;
import net.potatocloud.node.service.ServiceStartQueue;
import net.potatocloud.node.setup.SetupManager;
import net.potatocloud.node.template.TemplateManager;
import net.potatocloud.node.utils.PortCheckUtil;
import net.potatocloud.node.version.UpdateChecker;
import net.potatocloud.node.version.VersionFile;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

@Getter
public class Node extends CloudAPI {

    private final long startupTime;
    private final NodeConfig config;
    private final CommandManager commandManager;
    private final Console console;
    private final Logger logger;
    private final ScreenManager screenManager;
    private final SetupManager setupManager;
    private final UpdateChecker updateChecker;
    private final PacketManager packetManager;
    private final NetworkServer server;
    private final EventManager eventManager;
    private final CloudPlayerManager playerManager;
    private final TemplateManager templateManager;
    private final ServiceGroupManager groupManager;
    private final PlatformManagerImpl platformManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;
    private final ServiceManagerImpl serviceManager;
    private final ServiceStartQueue serviceStartQueue;

    private final String previousVersion;
    private boolean isStopping;

    public Node(long startupTime) {
        this.startupTime = startupTime;

        config = new NodeConfig();

        if (PortCheckUtil.isPortInUse(config.getNodeHost(), config.getNodePort())) {
            System.err.println("The configured node port is already in use. Is another instance of potatocloud already running on this port?");
            System.exit(0);
        }

        previousVersion = VersionFile.getVersion();
        VersionFile.create();

        commandManager = new CommandManager();
        console = new Console(commandManager, this);
        console.start();
        logger = new Logger(console, Path.of(config.getLogsFolder()));
        new ExceptionMessageHandler(logger);

        screenManager = new ScreenManager(console, logger);
        final Screen nodeScreen = new Screen(Screen.NODE_SCREEN);
        screenManager.addScreen(nodeScreen);
        screenManager.setCurrentScreen(nodeScreen);

        setupManager = new SetupManager();

        updateChecker = new UpdateChecker(logger);
        updateChecker.checkForUpdates();

        packetManager = new PacketManager();
        server = new NettyNetworkServer(packetManager);
        server.start(config.getNodeHost(), config.getNodePort());
        logger.info("NetworkServer started using &aNetty &7on &a" + config.getNodeHost() + "&8:&a" + config.getNodePort());

        eventManager = new ServerEventManager(server);
        playerManager = new CloudPlayerManagerImpl(server);
        templateManager = new TemplateManager(logger, Path.of(config.getTemplatesFolder()));
        groupManager = new ServiceGroupManagerImpl(Path.of(config.getGroupsFolder()), server);
        ((ServiceGroupManagerImpl) groupManager).loadGroups();

        if (!groupManager.getAllServiceGroups().isEmpty()) {
            logger.info("Loaded &a" + groupManager.getAllServiceGroups().size() + "&7 Service Groups&8:");
            for (ServiceGroup group : groupManager.getAllServiceGroups()) {
                logger.info("&8» &a" + group.getName());
            }
        }

        platformManager = new PlatformManagerImpl(logger, server);
        downloadManager = new DownloadManager(Path.of(config.getPlatformsFolder()), logger);
        cacheManager = new CacheManager(logger);

        ServiceDefaultFiles.copyDefaultFiles(logger, config, getClass().getClassLoader());
        serviceManager = new ServiceManagerImpl(
                config, logger, server, eventManager, groupManager, screenManager, templateManager, platformManager, downloadManager, cacheManager, console
        );
        serviceStartQueue = new ServiceStartQueue(groupManager, serviceManager);

        registerCommands();

        logger.info("Startup completed in &a" + (System.currentTimeMillis() - startupTime) + "ms &8| &7Use &8'&ahelp&8' &7to see available commands");

        serviceStartQueue.start();
    }

    private void registerCommands() {
        commandManager.registerCommand(new GroupCommand(logger, groupManager));
        commandManager.registerCommand(new ServiceCommand(logger, serviceManager, groupManager));
        commandManager.registerCommand(new ShutdownCommand(this));
        commandManager.registerCommand(new PlatformCommand(logger, platformManager, downloadManager));
        commandManager.registerCommand(new ClearCommand(console));
        commandManager.registerCommand(new HelpCommand(logger, commandManager));
        commandManager.registerCommand(new PlayerCommand(logger, playerManager, serviceManager));
        commandManager.registerCommand(new InfoCommand(logger));
    }

    @SneakyThrows
    public void shutdown() {
        isStopping = true;
        logger.info("&7Starting node &cshutdown&7...");
        serviceStartQueue.close();

        for (Service service : serviceManager.getAllServices()) {
            ((ServiceImpl) service).shutdownBlocking();
        }

        for (NetworkConnection connectedSession : server.getConnectedSessions()) {
            connectedSession.close();
        }
        server.shutdown();

        FileUtils.deleteDirectory(Path.of(config.getTempServicesFolder()).toFile());

        logger.info("&7Shutdown complete. Goodbye!");
        console.close();
    }

    public long getUptime() {
        return System.currentTimeMillis() - startupTime;
    }

    public static Node getInstance() {
        return (Node) CloudAPI.getInstance();
    }

    @Override
    public ServiceGroupManager getServiceGroupManager() {
        return groupManager;
    }
}
