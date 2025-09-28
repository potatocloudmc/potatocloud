package net.potatocloud.node.service;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.service.PreparedServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packets.service.ServiceRemovePacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformManagerImpl;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.template.TemplateManager;
import org.apache.commons.io.FileUtils;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
public class ServiceImpl implements Service {

    private final int serviceId;
    private final int port;
    private final ServiceGroup serviceGroup;
    private final NodeConfig config;
    private final Logger logger;

    private final List<String> logs = new ArrayList<>();

    private final NetworkServer server;
    private final ScreenManager screenManager;
    private final TemplateManager templateManager;
    private final PlatformManagerImpl platformManager;
    private final DownloadManager downloadManager;

    private final EventManager eventManager;
    private final ServiceManager serviceManager;
    private final Console console;

    private final Set<Property> properties;
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

    @Setter
    private ServiceProcessChecker processChecker;

    public ServiceImpl(
            int serviceId,
            int port,
            ServiceGroup serviceGroup,
            NodeConfig config,
            Logger logger,
            NetworkServer server,
            ScreenManager screenManager,
            TemplateManager templateManager,
            PlatformManagerImpl platformManager,
            DownloadManager downloadManager,
            EventManager eventManager,
            ServiceManager serviceManager,
            Console console
    ) {
        this.serviceId = serviceId;
        this.port = port;
        this.serviceGroup = serviceGroup;
        this.config = config;
        this.logger = logger;
        this.server = server;
        this.screenManager = screenManager;
        this.templateManager = templateManager;
        this.platformManager = platformManager;
        this.downloadManager = downloadManager;
        this.eventManager = eventManager;
        this.serviceManager = serviceManager;
        this.console = console;

        maxPlayers = serviceGroup.getMaxPlayers();
        properties = new HashSet<>(serviceGroup.getProperties());

        screen = new Screen(getName());
        screenManager.addScreen(screen);
    }

    @Override
    public String getName() {
        return serviceGroup.getName() + config.getSplitter() + serviceId;
    }

    public int getUsedMemory() {
        if (serverProcess == null || !serverProcess.isAlive()) {
            return 0;
        }

        final SystemInfo info = new SystemInfo();
        final OSProcess process = info.getOperatingSystem().getProcess((int) serverProcess.pid());

        if (process != null) {
            long usedBytes = process.getResidentSetSize();
            return (int) (usedBytes / 1024 / 1024);
        }
        return 0;
    }

    @SneakyThrows
    public void start() {
        if (isOnline()) {
            return;
        }

        status = ServiceStatus.STARTING;
        startTimestamp = System.currentTimeMillis();

        // create service folder
        final Path staticFolder = Path.of(config.getStaticFolder());
        final Path tempFolder = Path.of(config.getTempServicesFolder());
        directory = serviceGroup.isStatic() ? staticFolder.resolve(getName()) : tempFolder.resolve(getName());

        if (!serviceGroup.isStatic()) {
            if (Files.exists(directory)) {
                FileUtils.deleteQuietly(directory.toFile());
            }
        }

        Files.createDirectories(directory);

        // copy templates
        for (String templateName : serviceGroup.getServiceTemplates()) {
            templateManager.copyTemplate(templateName, directory);
        }

        // copy cloud plugin from data folder into server plugins folder
        final Path pluginsFolder = directory.resolve("plugins");
        Files.createDirectories(pluginsFolder);

        String pluginName = "";
        if (serviceGroup.getPlatform().isBukkitBased()) {
            pluginName = "potatocloud-plugin-spigot.jar";
        } else if (serviceGroup.getPlatform().isVelocityBased()) {
            pluginName = "potatocloud-plugin-velocity.jar";
        } else if (serviceGroup.getPlatform().isLimboBased()) {
            pluginName = "potatocloud-plugin-limbo.jar";
        }

        FileUtils.copyFile(Path.of(config.getDataFolder(), pluginName).toFile(), pluginsFolder.resolve(pluginName).toFile(), StandardCopyOption.REPLACE_EXISTING);

        // download the platform of the service
        final Platform platform = serviceGroup.getPlatform();
        final PlatformVersion version = serviceGroup.getPlatformVersion();

        downloadManager.downloadPlatformVersion(platform, platform.getVersion(serviceGroup.getPlatformVersionName()));

        final Path cacheFolder = Node.getInstance().getCacheManager().preCachePlatform(serviceGroup);

        Node.getInstance().getCacheManager().copyCacheToService(serviceGroup, cacheFolder, directory);

        // copy server file
        final File platformFile = PlatformUtils.getPlatformJarFile(platform, version);
        final Path finalServerFilePath = directory.resolve("server.jar");

        FileUtils.copyFile(platformFile, finalServerFilePath.toFile());

        // execute the prepare steps
        for (String step : platform.getPrepareSteps()) {
            platformManager.getStep(step).execute(this, platform, directory);
        }

        // create start arguments
        final ArrayList<String> args = new ArrayList<>();
        args.add(serviceGroup.getJavaCommand());
        args.add("-Xms" + serviceGroup.getMaxMemory() + "M");
        args.add("-Xmx" + serviceGroup.getMaxMemory() + "M");
        args.add("-Dpotatocloud.service.name=" + getName());
        args.add("-Dpotatocloud.node.port=" + config.getNodePort());

        args.addAll(ServicePerformanceFlags.DEFAULT_FLAGS);

        if (serviceGroup.getCustomJvmFlags() != null) {
            args.addAll(serviceGroup.getCustomJvmFlags());
        }

        args.add("-jar");
        args.add(finalServerFilePath.toAbsolutePath().toString());

        if (platform.isBukkitBased() && !version.isLegacy()) {
            args.add("-nogui");
        }

        if (platform.isLimboBased()) {
            args.add("--nogui");
        }

        // create and start the service process
        final ProcessBuilder processBuilder = new ProcessBuilder(args).directory(directory.toFile());
        serverProcess = processBuilder.start();

        processWriter = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
        processReader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));

        new Thread(() -> {
            try {
                String line;
                while ((line = processReader.readLine()) != null) {
                    logs.add(line);
                    screen.addLog(line);

                    if (screenManager.getCurrentScreen().getName().equals(getName())) {
                        console.println(line);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "ProcessReader-" + getName()).start();

        logger.info("Service &a" + this.getName() + "&7 is now starting&8... &8[&7Port&8: &a" + port + "&8, &7Group&8: &a" + serviceGroup.getName() + "&8]");
        eventManager.call(new PreparedServiceStartingEvent(this.getName()));
    }

    @Override
    public void shutdown() {
        if (status == ServiceStatus.STOPPED || status == ServiceStatus.STOPPING) {
            return;
        }
        new Thread(this::shutdownBlocking, "Shutdown-" + getName()).start();
    }

    @SneakyThrows
    public void shutdownBlocking() {
        if (status == ServiceStatus.STOPPED || status == ServiceStatus.STOPPING) {
            return;
        }

        if (processChecker != null) {
            processChecker.interrupt();
            processChecker = null;
        }

        logger.info("Stopping service &a" + getName() + "&7...");
        status = ServiceStatus.STOPPING;

        if (server != null && eventManager != null) {
            eventManager.call(new ServiceStoppingEvent(this.getName()));
        }

        final Platform platform = platformManager.getPlatform(serviceGroup.getPlatformName());
        executeCommand(platform.isProxy() ? "end" : "stop");

        if (processWriter != null) {
            processWriter.close();
            processWriter = null;
        }

        if (serverProcess != null) {
            final boolean finished = serverProcess.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                serverProcess.toHandle().destroyForcibly();
                serverProcess.waitFor();
            }
            serverProcess = null;
        }

        cleanup();
    }

    public void cleanup() {
        if (status == ServiceStatus.STOPPED) {
            return;
        }

        status = ServiceStatus.STOPPED;
        startTimestamp = 0L;

        ((ServiceManagerImpl) serviceManager).removeService(this);

        screenManager.removeScreen(screen);

        if (screenManager.getCurrentScreen().getName().equals(getName())) {
            screenManager.switchScreen(Screen.NODE_SCREEN);
        }

        if (server != null) {
            server.broadcastPacket(new ServiceRemovePacket(this.getName(), this.getPort()));

            eventManager.call(new ServiceStoppedEvent(this.getName()));
        }

        if (!serviceGroup.isStatic()) {
            if (Files.exists(directory)) {
                if (!FileUtils.deleteQuietly(directory.toFile())) {
                    logger.error("Temp directory for " + getName() + " could not be deleted! The service might still be running");
                }
            }
        }

        logger.info("Service &a" + getName() + " &7has been stopped");
    }


    @Override
    @SneakyThrows
    public boolean executeCommand(String command) {
        if (serverProcess == null || !serverProcess.isAlive() || processWriter == null) {
            return false;
        }
        processWriter.write(command);
        processWriter.newLine();
        processWriter.flush();
        return true;
    }

    @Override
    @SneakyThrows
    public void copy(String template, String filter) {
        final Path templatesFolder = Path.of(config.getTemplatesFolder());
        Path targetPath = templatesFolder.resolve(template);
        Path sourcePath = directory;

        // a filter was set
        if (filter != null && filter.startsWith("/")) {
            // remove the / symbol
            sourcePath = directory.resolve(filter.substring(1));
            targetPath = targetPath.resolve(filter.substring(1));
        }

        if (!Files.exists(sourcePath)) {
            return;
        }

        if (!Files.exists(targetPath)) {
            templateManager.createTemplate(targetPath.toFile().getName());
        }

        try {
            FileUtils.copyDirectory(sourcePath.toFile(), targetPath.toFile());
        } catch (FileSystemException ignored) {

        }
    }

    @SneakyThrows
    public List<String> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    @Override
    public String getPropertyHolderName() {
        return getName();
    }
}
