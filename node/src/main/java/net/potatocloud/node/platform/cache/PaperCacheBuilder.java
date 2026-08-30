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

public final class PaperCacheBuilder implements CacheBuilder {

    private static final List<String> COPY_DIRECTORIES = List.of("cache", "libraries", "versions");

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public CompletableFuture<Void> build(Group group, Platform platform, PlatformVersion version, Path cacheFolder) {
        return CompletableFuture.runAsync(() -> {
            final Path tempDirectory = cacheFolder.resolve("temp");

            try {
                Files.createDirectories(tempDirectory);

                final Path platformJar = PlatformUtils.getPlatformJarPath(platform, version);

                final ProcessBuilder processBuilder = new ProcessBuilder(
                        group.javaCommand(),
                        "-Dpaperclip.patchonly=true",
                        "-jar",
                        platformJar.toAbsolutePath().toString()
                );

                processBuilder.directory(tempDirectory.toFile());

                final Process process = processBuilder.start();

                final int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new RuntimeException("Failed to build Paper platform cache for group: " + group.name() + " (exit code: " + exitCode + ")");
                }

                copyGeneratedFiles(tempDirectory, cacheFolder);

            } catch (Exception e) {
                throw new RuntimeException("Failed to build Paper cache", e);
            } finally {
                FileUtils.deleteDirectory(tempDirectory);
            }
        }, executor);
    }

    @Override
    public void copyToService(Path cacheDirectory, Path serviceDirectory) {
        copyGeneratedFiles(cacheDirectory, serviceDirectory);
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
        return !version.legacy();
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
