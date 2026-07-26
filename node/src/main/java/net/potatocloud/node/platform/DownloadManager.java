package net.potatocloud.node.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.Node;
import net.potatocloud.node.platform.parser.LeafBuildParser;
import net.potatocloud.node.platform.parser.McJarsBuildParser;
import net.potatocloud.node.platform.parser.PaperBuildParser;
import net.potatocloud.node.platform.parser.PurpurBuildParser;
import net.potatocloud.node.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RequiredArgsConstructor
public class DownloadManager {

    private final Path platformsDirectory;
    private final Logger logger;

    private static final List<BuildParser> PARSERS = List.of(
            new PaperBuildParser("paper"),
            new PaperBuildParser("velocity"),
            new PurpurBuildParser(),
            new LeafBuildParser(),
            new McJarsBuildParser("mcjars-fabric", "FABRIC"),
            new McJarsBuildParser("mcjars-neoforge", "NEOFORGE")
    );

    public void downloadPlatformVersion(Platform platform, PlatformVersion version) {
        if (platform == null) {
            logger.info("&cThis platform does not exist");
            return;
        }

        if (version == null) {
            logger.info("&cThis version does not exist");
            return;
        }

        if (!Files.exists(platformsDirectory)) {
            try {
                Files.createDirectories(platformsDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create platforms directory: " + platformsDirectory, e);
            }
        }

        final Path platformJarPath = PlatformUtils.getPlatformJarPath(platform, version);

        if (version.local()) {
            if (Files.notExists(platformJarPath)) {
                logger.error("Platform &a" + platform.name() + " &7version &a" + version.name() + " &7does not exist!");
                return;
            }
            return;
        }

        final BuildParser parser = PARSERS.stream()
                .filter(p -> p.getName().equalsIgnoreCase(platform.parser()))
                .findFirst()
                .orElse(null);

        if ((version.downloadUrl() == null || version.downloadUrl().isEmpty()) && parser != null) {
            parser.parse(version, platform.downloadUrl());
        }

        if (version.downloadUrl() == null || version.downloadUrl().isEmpty()) {
            logger.info("&cVersion &a" + version.name() + " &7has no download url!");
            return;
        }

        final Path platformDirectory = PlatformUtils.getDirectoryOfPlatform(platform, version);
        final boolean missingLibraries = platform.neoForgeBased() && !Files.exists(platformDirectory.resolve("libraries"));

        if (Files.notExists(platformJarPath) || missingLibraries) {
            download(platform, version, platformJarPath);
            return;
        }

        final boolean autoUpdate = Node.instance().config().service().autoUpdatePlatforms();
        if (autoUpdate && needsUpdate(version, platformJarPath)) {
            logger.info("Platform &a" + platform.name() + " &7is outdated! Downloading update&8...");
            download(platform, version, platformJarPath);
        }
    }

    private void download(Platform platform, PlatformVersion version, Path platformJarPath) {
        logger.info("&7Downloading platform &a" + platform.name() + "&7 version &a" + version.name());

        if (version.downloadUrl() == null || version.downloadUrl().isEmpty()) {
            logger.error("No download URL found for platform: " + platform.name());
            return;
        }

        if (version.downloadUrl().toLowerCase().endsWith(".zip")) {
            downloadServerArchive(version.downloadUrl(), platformJarPath);
        } else {
            FileUtils.downloadFile(version.downloadUrl(), platformJarPath);
        }

        logger.info("&7Finished downloading platform &a" + platform.name() + "&7 version &a" + version.name());
    }

    private void downloadServerArchive(String downloadUrl, Path platformJarPath) {
        final Path platformDirectory = platformJarPath.getParent();
        final Path archive = platformDirectory.resolve("server.zip");
        final Path serverJar = platformDirectory.resolve("server.jar");

        try {
            FileUtils.downloadFile(downloadUrl, archive);
            FileUtils.unzip(archive, platformDirectory);

            if (Files.notExists(serverJar)) {
                throw new IllegalStateException("Downloaded server archive does not contain server.jar: " + downloadUrl);
            }

            Files.move(serverJar, platformJarPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy server archive from URL: " + downloadUrl, e);
        } finally {
            try {
                Files.deleteIfExists(archive);
            } catch (IOException ignored) {
            }
        }
    }

    private boolean needsUpdate(PlatformVersion version, Path platformJarPath) {
        final String versionHash = version.fileHash();
        if (versionHash == null || versionHash.isEmpty()) {
            return false;
        }

        final String currentHash = version.platform().hashType().equals("md5")
                ? HashUtils.md5(platformJarPath)
                : HashUtils.sha256(platformJarPath);

        return !currentHash.equalsIgnoreCase(versionHash);
    }
}
