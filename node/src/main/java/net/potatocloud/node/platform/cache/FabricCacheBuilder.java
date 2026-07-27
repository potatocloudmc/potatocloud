package net.potatocloud.node.platform.cache;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.platform.PlatformUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FabricCacheBuilder implements CacheBuilder {

    private static final List<String> COPY_DIRECTORIES = List.of(".fabric", "libraries");

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public CompletableFuture<Void> build(Group group, Platform platform, PlatformVersion version, Path cacheFolder) {
        return CompletableFuture.runAsync(() -> {
            final Path tempDirectory = cacheFolder.resolve("temp");

            try {
                Files.createDirectories(tempDirectory);

                final ProcessBuilder processBuilder = new ProcessBuilder(
                        group.javaCommand(),
                        "-jar",
                        PlatformUtils.getPlatformJarPath(platform, version).toAbsolutePath().toString(),
                        "-nogui"
                );

                processBuilder.directory(tempDirectory.toFile());
                processBuilder.redirectErrorStream(true);
                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);

                final Process process = processBuilder.start();

                final int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new RuntimeException("Failed to build Fabric platform cache for group: " + group.name() + " (exit code: " + exitCode + ")");
                }

                copyGeneratedFiles(tempDirectory, cacheFolder);
            } catch (Exception e) {
                throw new RuntimeException("Failed to build Fabric cache", e);
            } finally {
                FileUtils.deleteDirectory(tempDirectory);
            }
        }, executor);
    }

    @Override
    public void copyToService(Path cacheFolder, Path serviceDir) {
        copyGeneratedFiles(cacheFolder, serviceDir);
    }

    private void copyGeneratedFiles(Path source, Path target) {
        for (String directory : COPY_DIRECTORIES) {
            final Path sourceDirectory = source.resolve(directory);

            if (!Files.exists(sourceDirectory)) {
                continue;
            }

            FileUtils.copyDirectory(sourceDirectory, target.resolve(directory));
        }
    }

    @Override
    public boolean supports(PlatformVersion version) {
        return true;
    }
}
