package net.potatocloud.node.service;

import lombok.Getter;
import lombok.Setter;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.service.PreparedServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.packets.service.ServiceRemovePacket;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformManagerImpl;
import net.potatocloud.node.platform.PlatformPrepareSteps;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.template.TemplateManager;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class ServiceImpl implements Service {

    private final int serviceId;
    private final UUID serviceUUID = UUID.randomUUID();
    private final int port;
    private final ServiceGroup group;
    private final NodeConfig config;
    private final Logger logger;

    private final List<String> logs;

    private final NetworkServer server;
    private final ScreenManager screenManager;
    private final TemplateManager templateManager;
    private final PlatformManagerImpl platformManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;

    private final EventManager eventManager;
    private final ServiceManager serviceManager;
    private final Console console;

    private final Map<String, Property<?>> propertyMap;
    private final Screen screen;

    @Setter
    private int maxPlayers;

    @Setter
    private ServiceStatus status = ServiceStatus.STOPPED;

    private long startTimestamp;
    private Path directory;

    private Process serverProcess;
    private BufferedWriter processWriter;
    private BufferedReader processReader;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Setter
    private ServiceProcessChecker processChecker;

    private ServiceProcessOutputReader outputReader;

    public ServiceImpl(
            int serviceId,
            int port,
            ServiceGroup group,
            NodeConfig config,
            Logger logger,
            NetworkServer server,
            ScreenManager screenManager,
            TemplateManager templateManager,
            PlatformManagerImpl platformManager,
            DownloadManager downloadManager,
            CacheManager cacheManager,
            EventManager eventManager,
            ServiceManager serviceManager,
            Console console
    ) {
        this.serviceId = serviceId;
        this.port = port;
        this.group = group;
        this.config = config;
        this.logger = logger;
        this.logs = new ArrayList<>();
        this.server = server;
        this.screenManager = screenManager;
        this.templateManager = templateManager;
        this.platformManager = platformManager;
        this.downloadManager = downloadManager;
        this.cacheManager = cacheManager;
        this.eventManager = eventManager;
        this.serviceManager = serviceManager;
        this.console = console;

        maxPlayers = group.getMaxPlayers();
        propertyMap = new HashMap<>(group.getPropertyMap());

        screen = new Screen(getName());
        screenManager.register(screen);
    }

    @Override
    public UUID getServiceUuid() {
        return serviceUUID;
    }

    @Override
    public String getName() {
        return group.getName() + "-" + serviceId;
    }

    @Override
    public ServiceGroup getServiceGroup() {
        return group;
    }

    public int getUsedMemory() {
        if (!isAlive()) {
            return 0;
        }

        final OSProcess process = new SystemInfo()
                .getOperatingSystem()
                .getProcess((int) serverProcess.pid());

        if (process == null) {
            return 0;
        }

        final long usedBytes = process.getResidentSetSize();
        return (int) (usedBytes / 1024 / 1024);
    }

    public void start() {
        if (isOnline()) {
            return;
        }

        status = ServiceStatus.STARTING;
        startTimestamp = System.currentTimeMillis();

        directory = getDirectory();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create service directory: " + directory, e);
        }

        for (String template : group.getServiceTemplates()) {
            templateManager.copyTemplate(template, directory);
        }

        final Path pluginsFolder = directory.resolve("plugins");
        try {
            Files.createDirectories(pluginsFolder);
            final String pluginName = getPlatformPluginName();
            Files.copy(
                    Path.of(config.getDataFolder(), pluginName),
                    pluginsFolder.resolve(pluginName),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to set up plugins folder for service " + getName(), e);
        }

        final Platform platform = group.getPlatform();
        final PlatformVersion version = group.getPlatformVersion();

        downloadManager.downloadPlatformVersion(platform, platform.getVersion(group.getPlatformVersionName()));

        final Path cacheFolder = cacheManager.preCachePlatform(group);
        cacheManager.copyCacheToService(group, cacheFolder, directory);

        try {
            Files.copy(
                    PlatformUtils.getPlatformJarPath(platform, version),
                    directory.resolve("server.jar"),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy platform jar for service" + getName(), e);
        }

        for (String step : platform.getPrepareSteps()) {
            PlatformPrepareSteps.getStep(step).execute(this, platform, directory);
        }

        final List<String> startArguments = getStartArguments();
        try {
            serverProcess = new ProcessBuilder(startArguments)
                    .directory(directory.toFile())
                    .start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server process for service " + getName(), e);
        }

        processWriter = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
        processReader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));

        outputReader = new ServiceProcessOutputReader(serverProcess, processReader, this);
        outputReader.start();

        logger.info("Service &a" + getName() + "&7 is now starting&8... &8[&7Port&8: &a" + port + "&8, &7Group&8: &a" + group.getName() + "&8]");
        eventManager.call(new PreparedServiceStartingEvent(getName()));
    }

    private Path getDirectory() {
        return group.isStatic()
                ? Path.of(config.getStaticFolder()).resolve(getName())
                : Path.of(config.getTempServicesFolder()).resolve(getName() + "-" + UUID.randomUUID());
    }

    private String getPlatformPluginName() {
        final Platform platform = group.getPlatform();

        if (platform.isBukkitBased()) {
            return group.getPlatformVersion().isLegacy()
                    ? "potatocloud-plugin-spigot-legacy.jar"
                    : "potatocloud-plugin-spigot.jar";
        } else if (platform.isVelocityBased()) {
            return "potatocloud-plugin-velocity.jar";
        } else if (platform.isLimboBased()) {
            return "potatocloud-plugin-limbo.jar";
        } else {
            logger.error("No Plugin found for platform " + platform.getName());
            return "";
        }
    }

    private List<String> getStartArguments() {
        final List<String> args = new ArrayList<>();
        args.add(group.getJavaCommand());
        args.add("-Xms" + group.getMaxMemory() + "M");
        args.add("-Xmx" + group.getMaxMemory() + "M");
        args.add("-Dpotatocloud.service.name=" + getName());
        args.add("-Dpotatocloud.node.port=" + config.getNodePort());

        args.addAll(ServicePerformanceFlags.DEFAULT_FLAGS);

        if (group.getCustomJvmFlags() != null) {
            args.addAll(group.getCustomJvmFlags());
        }

        args.add("-jar");
        args.add(directory.resolve("server.jar").toAbsolutePath().toString());

        if (group.getPlatform().isBukkitBased() && !group.getPlatformVersion().isLegacy()) {
            args.add("-nogui");
        }

        if (group.getPlatform().isLimboBased()) {
            args.add("--nogui");
        }

        return args;
    }

    public void addLog(String log) {
        logs.add(log);
        screen.addLog(log);

        console.println(log);

        if (screenManager.getCurrentScreen().name().equals(getName())) {
            console.println(log);
        }
    }

    @Override
    public void shutdown() {
        shutdownAsync();
    }

    public CompletableFuture<Void> shutdownAsync() {
        return CompletableFuture.runAsync(this::shutdownBlocking, executorService);
    }

    public void shutdownBlocking() {
        if (status == ServiceStatus.STOPPED || status == ServiceStatus.STOPPING) {
            return;
        }

        status = ServiceStatus.STOPPING;

        logger.info("Service &a" + getName() + "&7 is now stopping&8...");
        eventManager.call(new ServiceStoppingEvent(getName()));

        if (processChecker != null) {
            processChecker.interrupt();
            processChecker = null;
        }

        final Platform platform = platformManager.getPlatform(group.getPlatformName());

        try {
            executeCommand(platform.isProxy() ? "end" : "stop");

            if (serverProcess != null) {
                boolean exited = serverProcess.waitFor(config.getKillTimeout(), TimeUnit.SECONDS);

                if (!exited) {
                    logger.debug("Service &a" + getName() + " &7did not stop in time, destroying process&8...");

                    serverProcess.destroy();

                    if (!serverProcess.waitFor(2, TimeUnit.SECONDS)) {
                        logger.debug("Service &a" + getName() + " &7still alive, forcing shutdown&8...");
                        serverProcess.destroyForcibly();
                        serverProcess.waitFor();
                    }
                }
            }

            if (processWriter != null) {
                processWriter.close();
                processWriter = null;
            }

            if (outputReader != null) {
                outputReader.interrupt();
                outputReader = null;
            }

            if (processReader != null) {
                processReader.close();
                processReader = null;
            }

        } catch (Exception e) {
            logger.error("Failed to stop service &a" + getName() + "&8: &7" + e.getMessage());
        }

        serverProcess = null;
        cleanup();
    }

    public void cleanup() {
        if (status == ServiceStatus.STOPPED) {
            return;
        }

        status = ServiceStatus.STOPPED;
        startTimestamp = 0L;

        ((ServiceManagerImpl) serviceManager).removeService(this);

        screenManager.unregister(screen.name());

        if (screenManager.getCurrentScreen().name().equals(getName())) {
            screenManager.switchTo(Screen.NODE_SCREEN);
        }

        if (server != null) {
            server.generateBroadcast().broadcast(new ServiceRemovePacket(this.getName(), this.getPort()));
            eventManager.call(new ServiceStoppedEvent(this.getName()));
        }

        if (!group.isStatic() && Files.exists(directory)) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (RuntimeException e) {
                logger.error("Temp directory for " + getName() + " could not be deleted! The service might still be running");
            }
        }

        logger.info("Service &a" + getName() + " &7has been stopped");
    }

    @Override
    public boolean executeCommand(String command) {
        if (!isAlive() || processWriter == null) {
            return false;
        }

        try {
            processWriter.write(command);
            processWriter.newLine();
            processWriter.flush();
        } catch (IOException e) {
            logger.error("Failed to send command to service " + getName());
            return false;
        }

        return true;
    }

    private boolean isAlive() {
        return serverProcess != null && serverProcess.isAlive();
    }

    @Override
    public void copy(String template, String filter) {
        final Path templatesFolder = Path.of(config.getTemplatesFolder());
        Path targetPath = templatesFolder.resolve(template);
        Path sourcePath = directory;

        if (filter != null && filter.startsWith("/")) {
            sourcePath = directory.resolve(filter.substring(1));
            targetPath = targetPath.resolve(filter.substring(1));
        }

        if (!Files.exists(sourcePath)) {
            return;
        }

        if (!Files.exists(targetPath)) {
            templateManager.createTemplate(targetPath.toFile().getName());
        }

        FileUtils.copyDirectory(sourcePath, targetPath);
    }

    @Override
    public String getPropertyHolderName() {
        return getName();
    }
}
