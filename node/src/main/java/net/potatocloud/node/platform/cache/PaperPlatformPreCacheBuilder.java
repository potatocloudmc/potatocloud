package net.potatocloud.node.platform.cache;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.platform.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PaperPlatformPreCacheBuilder implements PlatformPreCacheBuilder {

    @Override
    public void buildCache(Platform platform, PlatformVersion version, ServiceGroup group, Path cacheFolder) {
        try {
            final Path platformJarPath = PlatformUtils.getPlatformJarPath(platform, version);

            // Create a temporary folder for cache generation
            final Path tempDir = cacheFolder.resolve("temp");
            Files.createDirectories(tempDir);

            final ArrayList<String> args = new ArrayList<>();
            args.add(group.getJavaCommand());
            args.add("-Dpaperclip.patchonly=true");
            args.add("-jar");
            args.add(platformJarPath.toFile().getAbsolutePath());

            // Run the server process to generate cache
            final ProcessBuilder processBuilder = new ProcessBuilder(args).directory(tempDir.toFile());
            final Process process = processBuilder.start();
            process.waitFor();

            // Copy generated files to the actual cache folder
            final Path generatedCache = tempDir.resolve("cache");
            if (Files.exists(generatedCache)) {
                FileUtils.copyDirectory(generatedCache, cacheFolder.resolve("cache"));
            }

            final Path generatedLibraries = tempDir.resolve("libraries");
            if (Files.exists(generatedLibraries)) {
                FileUtils.copyDirectory(generatedLibraries, cacheFolder.resolve("libraries"));
            }

            final Path generatedVersions = tempDir.resolve("versions");
            if (Files.exists(generatedVersions)) {
                FileUtils.copyDirectory(generatedVersions, cacheFolder.resolve("versions"));
            }

            FileUtils.deleteDirectory(tempDir);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to build Paper platform cache for group: " + group.getName(), e);
        }
    }

    public void copyCacheToService(Path cacheFolder, Path serviceDir) {
        if (cacheFolder == null) {
            return;
        }

        // Copy cached folders to the service directory
        final Path cachedCache = cacheFolder.resolve("cache");
        if (Files.exists(cachedCache)) {
            FileUtils.copyDirectory(cachedCache, serviceDir.resolve("cache"));
        }

        final Path cachedLibraries = cacheFolder.resolve("libraries");
        if (Files.exists(cachedLibraries)) {
            FileUtils.copyDirectory(cachedLibraries, serviceDir.resolve("libraries"));
        }

        final Path cachedVersions = cacheFolder.resolve("versions");
        if (Files.exists(cachedVersions)) {
            FileUtils.copyDirectory(cachedVersions, serviceDir.resolve("versions"));
        }
    }
}
