package net.potatocloud.node.platform;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.Node;
import net.potatocloud.node.platform.parser.LeafBuildParser;
import net.potatocloud.node.platform.parser.PaperBuildParser;
import net.potatocloud.node.platform.parser.PurpurBuildParser;
import net.potatocloud.node.utils.HashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
public class DownloadManager {

    private final Path platformsDirectory;
    private final Logger logger;

    private static final List<BuildParser> PARSERS = List.of(
            new PaperBuildParser("paper"),
            new PaperBuildParser("velocity"),
            new PurpurBuildParser(),
            new LeafBuildParser()
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

        if (Files.notExists(platformJarPath)) {
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
        FileUtils.downloadFile(version.downloadUrl(), platformJarPath);
        logger.info("&7Finished downloading platform &a" + platform.name() + "&7 version &a" + version.name());
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
