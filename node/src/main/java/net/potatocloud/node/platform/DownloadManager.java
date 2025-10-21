package net.potatocloud.node.platform;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.parser.PaperBuildParser;
import net.potatocloud.node.platform.parser.PurpurBuildParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
public class DownloadManager {

    private final Path platformsFolder;
    private final Logger logger;

    private static final List<BuildParser> PARSERS = List.of(new PaperBuildParser("paper"), new PaperBuildParser("velocity"), new PurpurBuildParser());

    @SneakyThrows
    public void downloadPlatformVersion(Platform platform, PlatformVersion version) {
        if (platform == null) {
            logger.info("&cThis platform does not exist.");
            return;
        }

        if (!Files.exists(platformsFolder)) {
            Files.createDirectories(platformsFolder);
        }

        final File platformFile = PlatformUtils.getPlatformJarFile(platform, version);

        final BuildParser parser = PARSERS.stream()
                .filter(p -> p.getName().equalsIgnoreCase(platform.getParser()))
                .findFirst()
                .orElse(null);

        // use build parser to get the correct download url and hash if version has no download url
        if ((version.getDownloadUrl() == null || version.getDownloadUrl().isEmpty()) && parser != null) {
            parser.parse(version, platform.getDownloadUrl());
        }

        if (!platformFile.exists()) {
            download(platform, version, platformFile);
            return;
        }

        final boolean autoUpdate = Node.getInstance().getConfig().isPlatformAutoUpdate();
        if (autoUpdate && needsUpdate(version, platformFile)) {
            logger.info("Platform &a" + platform.getName() + " &7is outdated! Downloading update&8...");
            download(platform, version, platformFile);
        }
    }

    @SneakyThrows
    private void download(Platform platform, PlatformVersion version, File platformFile) {
        logger.info("&7Downloading platform &a" + platform.getName() + "&7 version &a" + version.getName());

        if (version.getDownloadUrl() == null || version.getDownloadUrl().isEmpty()) {
            logger.error("No download URL found for platform: " + platform.getName());
            return;

        }
        FileUtils.copyURLToFile(URI.create(version.getDownloadUrl()).toURL(), platformFile, 5000, 5000);
        logger.info("&7Finished downloading platform &a" + platform.getName() + "&7 version &a" + version.getName());
    }

    @SneakyThrows
    private boolean needsUpdate(PlatformVersion version, File platformFile) {
        final String versionHash = version.getFileHash();
        if (versionHash == null || versionHash.isEmpty()) {
            return false;
        }

        try (FileInputStream stream = new FileInputStream(platformFile)) {
            final String currentFileHash = version.getPlatform().getHashType().equals("md5")
                    ? DigestUtils.md5Hex(stream)
                    : DigestUtils.sha256Hex(stream);

            return !currentFileHash.equalsIgnoreCase(versionHash);
        }
    }
}
