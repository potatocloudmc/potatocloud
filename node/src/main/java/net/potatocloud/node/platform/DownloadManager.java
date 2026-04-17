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
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
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

        if (version.isLocal()) {
            if (Files.notExists(platformJarPath)) {
                logger.error("Platform &a" + platform.getName() + " &7version &a" + version.getName() + " &7does not exist!");
                return;
            }
            return;
        }

        final BuildParser parser = PARSERS.stream()
                .filter(p -> p.getName().equalsIgnoreCase(platform.getParser()))
                .findFirst()
                .orElse(null);

        // Use build parser to get the correct download url and hash if version has no download url
        if ((version.getDownloadUrl() == null || version.getDownloadUrl().isEmpty()) && parser != null) {
            parser.parse(version, platform.getDownloadUrl());
        }

        if (version.getDownloadUrl() == null || version.getDownloadUrl().isEmpty()) {
            logger.info("&cVersion &a" + version.getName() + " &7has no download url!");
            return;
        }

        if (Files.notExists(platformJarPath)) {
            download(platform, version, platformJarPath);
            return;
        }

        final boolean autoUpdate = Node.getInstance().getConfig().isPlatformAutoUpdate();
        if (autoUpdate && needsUpdate(version, platformJarPath)) {
            logger.info("Platform &a" + platform.getName() + " &7is outdated! Downloading update&8...");
            download(platform, version, platformJarPath);
        }
    }

    private void download(Platform platform, PlatformVersion version, Path platformJarPath) {
        logger.info("&7Downloading platform &a" + platform.getName() + "&7 version &a" + version.getName());

        if (version.getDownloadUrl() == null || version.getDownloadUrl().isEmpty()) {
            logger.error("No download URL found for platform: " + platform.getName());
            return;

        }
        FileUtils.downloadFile(version.getDownloadUrl(), platformJarPath);
        logger.info("&7Finished downloading platform &a" + platform.getName() + "&7 version &a" + version.getName());
    }

    private boolean needsUpdate(PlatformVersion version, Path platformJarPath) {
        // Check if the platform version file is outdated by comparing its hash with the latest version hash
        final String versionHash = version.getFileHash();
        if (versionHash == null || versionHash.isEmpty()) {
            return false;
        }

        try (FileInputStream stream = new FileInputStream(platformJarPath.toFile())) {
            final String currentFileHash = version.getPlatform().getHashType().equals("md5")
                    ? DigestUtils.md5Hex(stream)
                    : DigestUtils.sha256Hex(stream);

            // Returns true if the file is outdated
            return !currentFileHash.equalsIgnoreCase(versionHash);
        } catch (IOException e) {
            throw new RuntimeException("Failed to check for platform updates: " + platformJarPath, e);
        }
    }
}
