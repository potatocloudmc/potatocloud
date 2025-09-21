package net.potatocloud.node.platform.cache;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.utils.HashUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@RequiredArgsConstructor
public class CacheManager {

    private final Logger logger;

    private final Map<String, PlatformPreCacheBuilder> cacheBuilderMap = Map.of("paper", new PaperPlatformPreCacheBuilder());

    @SneakyThrows
    public Path preCachePlatform(ServiceGroup group) {
        final Platform platform = group.getPlatform();
        final PlatformVersion version = group.getPlatformVersion();

        final PlatformPreCacheBuilder builder = getPreCacher(platform.getPreCacheBuilder());

        // legacy versions are not supported by the paper pre cacher
        if (version.isLegacy() && builder instanceof PaperPlatformPreCacheBuilder) {
            return null;
        }

        final Path platformFolder = PlatformUtils.getPlatformFolder(platform, version);
        final File platformJarFile = PlatformUtils.getPlatformJarFile(platform, version);

        if (!platformJarFile.exists()) {
            return null;
        }

        final String jarHash = HashUtils.sha256(platformJarFile);
        final Path cacheFolder = platformFolder.resolve("cache-" + jarHash);

        if (cacheFolder.toFile().exists()) {
            // cache was already created and is up to date
            return cacheFolder;
        }

        // remove old cache folders
        for (File file : platformFolder.toFile().listFiles()) {
            if (file.isDirectory() && file.getName().startsWith("cache-")) {
                FileUtils.deleteDirectory(file);
            }
        }

        logger.info("Starting caching for " + platform.getName() + " version " + version.getName());
        cacheFolder.toFile().mkdirs();

        // start the pre cacher implementation of the platform
        builder.buildCache(platform, version, group, cacheFolder);
        logger.info("Finished caching for " + platform.getName() + " version " + version.getName());
        return cacheFolder;
    }

    public void copyCacheToService(ServiceGroup group, Path cacheFolder, Path serviceDir) {
        getPreCacher(group.getPlatform().getPreCacheBuilder()).copyCacheToService(cacheFolder, serviceDir);
    }

    private PlatformPreCacheBuilder getPreCacher(String name) {
        if (name == null) {
            return null;
        }
        return cacheBuilderMap.get(name.toLowerCase());
    }
}
