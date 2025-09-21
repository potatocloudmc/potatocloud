package net.potatocloud.node.platform.cache;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.PlatformUtils;
import net.potatocloud.node.utils.HashUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class CacheManager {

    private final Logger logger;

    private final Map<String, PlatformPreCacheBuilder> cacheBuilderMap = Map.of("paper", new PaperPlatformPreCacheBuilder());

    private final Set<String> runningCacheBuilders = Collections.synchronizedSet(new HashSet<>());

    public Path preCachePlatform(ServiceGroup group) {
        final Platform platform = group.getPlatform();
        final PlatformVersion version = group.getPlatformVersion();

        final PlatformPreCacheBuilder builder = getPreCacheBuilder(platform.getPreCacheBuilder());
        if (builder == null) {
            return null;
        }

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

        final String key = platform.getName() + "-" + version.getName() + "-" + jarHash;
        if (!runningCacheBuilders.add(key)) {
            return null;
        }

        if (cacheFolder.toFile().exists()) {
            // cache was already created and is up to date
            return cacheFolder;
        }

        try {
            // remove old cache folders
            for (File file : platformFolder.toFile().listFiles()) {
                if (file.isDirectory() && file.getName().startsWith("cache-")) {
                    FileUtils.deleteDirectory(file);
                }
            }

            logger.info("Started caching for &a" + platform.getName() + "&7 version &a" + version.getName());
            cacheFolder.toFile().mkdirs();

            // start the pre cacher implementation of the platform
            builder.buildCache(platform, version, group, cacheFolder);
            logger.info("Finished caching for " + platform.getName() + " version " + version.getName());

        } catch (Exception e) {
            logger.error("Caching failed for version " + version.getFullName());
        } finally {
            // make the builder free again even if it fails
            runningCacheBuilders.remove(key);
        }
        return cacheFolder;
    }

    public void copyCacheToService(ServiceGroup group, Path cacheFolder, Path serviceDir) {
        final PlatformPreCacheBuilder builder = getPreCacheBuilder(group.getPlatform().getPreCacheBuilder());
        if (builder != null) {
            builder.copyCacheToService(cacheFolder, serviceDir);
        }
    }

    private PlatformPreCacheBuilder getPreCacheBuilder(String name) {
        if (name == null) {
            return null;
        }
        return cacheBuilderMap.get(name.toLowerCase());
    }
}
