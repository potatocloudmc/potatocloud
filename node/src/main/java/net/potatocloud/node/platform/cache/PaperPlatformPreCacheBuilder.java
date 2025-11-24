package net.potatocloud.node.platform.cache;

import lombok.SneakyThrows;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.platform.PlatformUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PaperPlatformPreCacheBuilder implements PlatformPreCacheBuilder {

    @Override
    @SneakyThrows
    public void buildCache(Platform platform, PlatformVersion version, ServiceGroup group, Path cacheFolder) {
        final File platformJarFile = PlatformUtils.getPlatformJarFile(platform, version);

        // Create a temporary folder for cache generation
        final Path tempDir = cacheFolder.resolve("temp");
        tempDir.toFile().mkdirs();

        final ArrayList<String> args = new ArrayList<>();
        args.add(group.getJavaCommand());
        args.add("-Dpaperclip.patchonly=true");
        args.add("-jar");
        args.add(platformJarFile.getAbsolutePath());

        // Run the server process to generate cache
        final ProcessBuilder processBuilder = new ProcessBuilder(args).directory(tempDir.toFile());
        final Process process = processBuilder.start();
        process.waitFor();

        // copy generated files to the actual cache folder
        final Path generatedCache = tempDir.resolve("cache");
        if (Files.exists(generatedCache)) {
            FileUtils.copyDirectory(generatedCache.toFile(), cacheFolder.resolve("cache").toFile());
        }

        final Path generatedLibraries = tempDir.resolve("libraries");
        if (Files.exists(generatedLibraries)) {
            FileUtils.copyDirectory(generatedLibraries.toFile(), cacheFolder.resolve("libraries").toFile());
        }

        final Path generatedVersions = tempDir.resolve("versions");
        if (Files.exists(generatedVersions)) {
            FileUtils.copyDirectory(generatedVersions.toFile(), cacheFolder.resolve("versions").toFile());
        }

        FileUtils.deleteDirectory(tempDir.toFile());
    }

    @SneakyThrows
    public void copyCacheToService(Path cacheFolder, Path serviceDir) {
        if (cacheFolder == null) {
            return;
        }

        // Copy cached folders to the service directory
        final Path cachedCache = cacheFolder.resolve("cache");
        if (Files.exists(cachedCache)) {
            FileUtils.copyDirectory(cachedCache.toFile(), serviceDir.resolve("cache").toFile());
        }

        final Path cachedLibraries = cacheFolder.resolve("libraries");
        if (Files.exists(cachedLibraries)) {
            FileUtils.copyDirectory(cachedLibraries.toFile(), serviceDir.resolve("libraries").toFile());
        }

        final Path cachedVersions = cacheFolder.resolve("versions");
        if (Files.exists(cachedVersions)) {
            FileUtils.copyDirectory(cachedVersions.toFile(), serviceDir.resolve("versions").toFile());
        }
    }
}
