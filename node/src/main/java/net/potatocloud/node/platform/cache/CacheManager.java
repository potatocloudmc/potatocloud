package net.potatocloud.node.platform.cache;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.utils.HashUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CacheManager {

    private final Logger logger;

    private final Map<String, PlatformPreCacheBuilder> cacheBuilders = Map.of(
            "paper", new PaperPlatformPreCacheBuilder()
    );

    private final Set<String> runningCacheKeys = ConcurrentHashMap.newKeySet();

    public Path preCachePlatform(ServiceGroup group) {
        final Platform platform = group.getPlatform();
        final PlatformVersion version = group.getPlatformVersion();
        final String builderName = platform.getPreCacheBuilder();

        if (builderName == null) {
            // The platform does not have a pre-cacher
            return null;
        }

        final PlatformPreCacheBuilder builder = getPreCacheBuilder(platform.getPreCacheBuilder());

        if (version.isLegacy() && builder instanceof PaperPlatformPreCacheBuilder) {
            // Legacy versions not supported by Paper pre-cacher
            return null;
        }

        final Path platformDirectory = PlatformUtils.getDirectoryOfPlatform(platform, version);
        final Path platformJarPath = PlatformUtils.getPlatformJarPath(platform, version);

        if (Files.notExists(platformJarPath)) {
            return null;
        }

        final String jarHash = HashUtils.sha256(platformJarPath.toFile());
        final Path cacheDirectory = platformDirectory.resolve("cache-" + jarHash);
        final String cacheKey = platform.getName() + "-" + version.getName() + "-" + jarHash;


        if (Files.exists(cacheDirectory) || !runningCacheKeys.add(cacheKey)) {
            // Cache exists or is currently being built
            return cacheDirectory;
        }

        try {
            // Delete old cache directories
            FileUtils.list(platformDirectory).stream()
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("cache-"))
                    .forEach(FileUtils::deleteDirectory);

            logger.info("Started caching for &a" + platform.getName() + "&7 version &a" + version.getName());
            Files.createDirectories(cacheDirectory);

            // Start the pre cacher implementation of the platform
            builder.buildCache(platform, version, group, cacheDirectory);
            logger.info("Finished caching for &a" + platform.getName() + "&7 version &a" + version.getName());

        } catch (Exception e) {
            logger.error("Caching failed for version " + version.getFullName());
        } finally {
            runningCacheKeys.remove(cacheKey);
        }

        return cacheDirectory;
    }

    public void copyCacheToService(ServiceGroup group, Path cacheFolder, Path serviceDir) {
        final String builderName = group.getPlatform().getPreCacheBuilder();
        if (builderName != null) {
            // Copy pre-built cache into a service directory
            final PlatformPreCacheBuilder builder = getPreCacheBuilder(builderName);
            builder.copyCacheToService(cacheFolder, serviceDir);
        }
    }

    private PlatformPreCacheBuilder getPreCacheBuilder(String name) {
        final PlatformPreCacheBuilder builder = cacheBuilders.get(name.toLowerCase());
        if (builder == null) {
            throw new IllegalStateException("Unknown PlatformPreCacheBuilder: " + name);
        }
        return builder;
    }
}
