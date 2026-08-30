package net.potatocloud.node.platform.cache;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheManager {

    private final Logger logger;
    private final CacheRegistry registry;

    private final Map<String, CompletableFuture<Path>> runningCaches = new ConcurrentHashMap<>();

    public CacheManager(Logger logger) {
        this.logger = logger;
        this.registry = new CacheRegistry();
    }

    public CompletableFuture<Path> cache(Group group) {
        final Platform platform = group.platform();
        final PlatformVersion version = group.platformVersion();

        final String builderName = platform.preCacheBuilder();
        if (builderName == null) {
            return CompletableFuture.completedFuture(null);
        }

        final CacheBuilder builder = registry.get(builderName);

        if (!builder.supports(version)) {
            return CompletableFuture.completedFuture(null);
        }

        final Path platformDirectory = PlatformUtils.getDirectoryOfPlatform(platform, version);
        final Path platformJar = PlatformUtils.getPlatformJarPath(platform, version);

        if (!Files.exists(platformJar)) {
            return CompletableFuture.completedFuture(null);
        }

        final String hash = HashUtils.sha256(platformJar);
        final Path cacheDirectory = platformDirectory.resolve("cache-" + hash);

        if (Files.exists(cacheDirectory)) {
            return CompletableFuture.completedFuture(cacheDirectory);
        }

        final String cacheKey = platform.name() + "-" + version.name() + "-" + hash;

        final CompletableFuture<Path> running = runningCaches.get(cacheKey);
        if (running != null) {
            return running;
        }

        try {
            FileUtils.list(platformDirectory).stream()
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("cache-"))
                    .forEach(FileUtils::deleteDirectory);

            Files.createDirectories(cacheDirectory);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        logger.info("Started caching for &a" + platform.name() + "&7 version &a" + version.name());

        final CompletableFuture<Path> future = builder.build(group, platform, version, cacheDirectory).thenApply(v -> cacheDirectory);

        future.whenComplete((_, throwable) -> {
            runningCaches.remove(cacheKey);

            if (throwable == null) {
                logger.info("Finished caching for &a" + platform.name() + "&7 version &a" + version.name());
            } else {
                logger.error("Failed to cache &a" + platform.name() + "&7 version &a" + version.name() + " &c" + throwable.getMessage());

                FileUtils.deleteDirectory(cacheDirectory);
            }
        });

        runningCaches.put(cacheKey, future);

        return future;
    }

    public void copyToService(Group group, Path cacheDirectory, Path serviceDirectory) {
        if (cacheDirectory == null) {
            return;
        }

        final String builderName = group.platform().preCacheBuilder();

        if (builderName == null) {
            return;
        }

        registry.get(builderName).copyToService(cacheDirectory, serviceDirectory);
    }

    public void close() {
        registry.close();
    }
}
