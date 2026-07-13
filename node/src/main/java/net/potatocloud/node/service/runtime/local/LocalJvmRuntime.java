package net.potatocloud.node.service.runtime.local;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.FileUtils;
import net.potatocloud.network.security.SecurityConfig;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.platform.DownloadManager;
import net.potatocloud.node.platform.PlatformPrepareSteps;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.platform.cache.CacheManager;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.service.runtime.ServiceRuntime;
import net.potatocloud.node.template.TemplateManager;
import oshi.ffm.SystemInfo;
import oshi.software.os.OSProcess;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class LocalJvmRuntime implements ServiceRuntime {

    private final Group group;
    private final NodeConfig config;
    private final Logger logger;
    private final TemplateManager templateManager;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;
    private final Screen screen;

    private Path directory;
    private Process process;
    private OSProcess osProcess;
    private BufferedWriter processWriter;
    private BufferedReader processReader;

    public LocalJvmRuntime(
            Group group,
            NodeConfig config,
            Logger logger,
            TemplateManager templateManager,
            DownloadManager downloadManager,
            CacheManager cacheManager,
            Screen screen
    ) {
        this.group = group;
        this.config = config;
        this.logger = logger;
        this.templateManager = templateManager;
        this.downloadManager = downloadManager;
        this.cacheManager = cacheManager;
        this.screen = screen;
    }

    @Override
    public void prepare(Service service) {
        this.directory = resolveDirectory(service.name());

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create service directory: " + directory, e);
        }

        for (String template : group.templates()) {
            templateManager.copyTemplate(template, directory);
        }

        final Path pluginsFolder = directory.resolve("plugins");
        try {
            Files.createDirectories(pluginsFolder);

            final String pluginName = resolvePluginName();
            if (pluginName.isEmpty()) {
                return;
            }

            Files.copy(
                    Path.of(config.folders().data()).resolve(pluginName),
                    pluginsFolder.resolve(pluginName),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to install plugin for service " + service.name(), e);
        }

        final Platform platform = group.platform();
        downloadManager.downloadPlatformVersion(platform, platform.version(group.platformVersion().name()).get());

        final Path cacheDirectory = cacheManager.cache(group).join();
        cacheManager.copyToService(group, cacheDirectory, directory);

        final PlatformVersion version = group.platformVersion();
        try {
            Files.copy(
                    PlatformUtils.getPlatformJarPath(platform, version),
                    directory.resolve("server.jar"),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy server jar for service " + service.name(), e);
        }

        for (String stepName : group.platform().prepareSteps()) {
            final PrepareStep step = PlatformPrepareSteps.getStep(stepName);
            if (step != null) {
                step.data().put("group", group);
                step.data().put("port", service.port());

                step.execute(service.name(), group.platform(), directory);
            }
        }
    }

    @Override
    public void start(Service service) {
        final List<String> args = buildArguments(directory, service.name());

        try {
            process = new ProcessBuilder(args)
                    .directory(directory.toFile())
                    .start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server process for service " + service.name(), e);
        }

        osProcess = new SystemInfo().getOperatingSystem().getProcess((int) process.pid());

        processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Thread.startVirtualThread(() -> {
            try {
                String line;
                while (process.isAlive() && (line = processReader.readLine()) != null) {
                    screen.append(line);
                }
            } catch (IOException ignored) {
            }
        });
    }

    @Override
    public void stop(Service service) {
        if (process == null) {
            return;
        }

        executeCommand("stop");

        try {
            if (!process.waitFor(config.service().killTimeout(), TimeUnit.SECONDS)) {
                logger.debug("Service &a" + service.name() + " &7did not stop in time, destroying process&8...");
                process.destroyForcibly();
                process.waitFor();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                processWriter.close();
            } catch (IOException ignored) {
            }

            try {
                processReader.close();
            } catch (IOException ignored) {
            }

            process = null;
            osProcess = null;
            processWriter = null;
            processReader = null;
        }

        if (!group.staticServices() && directory != null && Files.exists(directory)) {
            FileUtils.deleteDirectory(directory);
        }
    }

    @Override
    public void executeCommand(String command) {
        if (!isAlive() || processWriter == null) {
            return;
        }

        try {
            processWriter.write(command);
            processWriter.newLine();
            processWriter.flush();
        } catch (IOException e) {
            logger.error("Failed to send command to process " + e.getMessage());
        }
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    @Override
    public int usedMemory() {
        if (!isAlive() || osProcess == null) {
            return 0;
        }
        return (int) (osProcess.getResidentMemory() / 1024 / 1024);
    }

    @Override
    public Optional<Path> directory() {
        return Optional.ofNullable(directory);
    }

    private Path resolveDirectory(String name) {
        if (group.staticServices()) {
            return Path.of(config.folders().staticServices()).resolve(name);
        }
        return Path.of(config.folders().tempServices()).resolve(name + "-" + UUID.randomUUID());
    }

    private List<String> buildArguments(Path directory, String name) {
        final List<String> args = new ArrayList<>();
        args.add(group.javaCommand());
        args.add("-Xms" + group.maxMemory() + "M");
        args.add("-Xmx" + group.maxMemory() + "M");
        args.add("-Dpotatocloud.service.name=" + name);
        args.add("-Dpotatocloud.node.host=" + config.node().host());
        args.add("-Dpotatocloud.node.port=" + config.node().port());

        final SecurityConfig security = config.security().toNetworkConfig();
        if (security.sslEnabled()) {
            args.add("-D" + SecurityConfig.SSL_ENABLED + "=true");
            args.add("-D" + SecurityConfig.SECURITY_DIRECTORY + "=" + security.securityDirectory().toAbsolutePath());
            args.add("-D" + SecurityConfig.REQUIRE_CLIENT_AUTH + "=" + security.requireClientAuth());
        }

        args.addAll(ServicePerformanceFlags.DEFAULT_FLAGS);

        if (group.customJvmFlags() != null) {
            args.addAll(group.customJvmFlags());
        }

        args.add("-jar");
        args.add(directory.resolve("server.jar").toAbsolutePath().toString());

        if (group.platform().bukkitBased() && !group.platformVersion().legacy()) {
            args.add("-nogui");
        }

        if (group.platform().limboBased()) {
            args.add("--nogui");
        }

        return args;
    }

    private String resolvePluginName() {
        final Platform platform = group.platform();
        final PlatformVersion version = group.platformVersion();

        if (platform.bukkitBased()) {
            return version.legacy()
                    ? "potatocloud-plugin-spigot-legacy.jar"
                    : "potatocloud-plugin-spigot.jar";
        }

        if (platform.velocityBased()) {
            return "potatocloud-plugin-velocity.jar";
        }

        if (platform.limboBased()) {
            return "potatocloud-plugin-limbo.jar";
        }

        logger.error("No plugin found for platform " + platform.name());
        return "";
    }
}
