package net.potatocloud.node.platform.steps;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.common.FileUtils;
import net.potatocloud.node.platform.AbstractPrepareStep;
import net.potatocloud.node.utils.GitHubClient;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InstallProxyForwardStep extends AbstractPrepareStep {

    private static final String REPOSITORY = "jirmjahu/proxyforward";

    @Override
    public void execute(String serviceName, Platform platform, Path serverDirectory) {
        if (!platform.moddedBased()) {
            return;
        }

        final Group group = (Group) data().get("group");
        final String loader = platform.fabricBased() ? "fabric" : "neoforge";
        final String minecraftVersion = group.platformVersion().resolvedName();
        final JsonNode release = GitHubClient.latestRelease(REPOSITORY);

        if (release == null || !release.isObject()) {
            throw new IllegalStateException("GitHub did not return the latest ProxyForward release");
        }

        final String releaseTag = release.path("tag_name").asString();
        if (releaseTag.isBlank()) {
            throw new IllegalStateException("Latest ProxyForward release has no version tag");
        }

        final String releaseVersion = releaseTag.startsWith("v")
                ? releaseTag.substring(1)
                : releaseTag;

        final String fileName = "proxyforward-" + loader + "-" + releaseVersion + "+" + minecraftVersion + ".jar";
        final String downloadUrl = downloadUrl(release, fileName);

        try {
            final Path modsDirectory = serverDirectory.resolve("mods");
            Files.createDirectories(modsDirectory);
            deletePreviousVersions(modsDirectory, loader);

            FileUtils.downloadFile(downloadUrl, modsDirectory.resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to install ProxyForward for service: " + serviceName, e);
        }
    }

    private String downloadUrl(JsonNode release, String fileName) {
        for (JsonNode asset : release.path("assets")) {
            if (asset.path("name").asString().equals(fileName)) {
                final String downloadUrl = asset.path("browser_download_url").asString();
                if (downloadUrl.isBlank()) {
                    throw new IllegalStateException("ProxyForward release asset has no download URL: " + fileName);
                }
                return downloadUrl;
            }
        }

        throw new IllegalStateException("Latest ProxyForward release does not contain " + fileName);
    }

    private void deletePreviousVersions(Path modsDirectory, String loader) throws IOException {
        final String fileNamePrefix = "proxyforward-" + loader + "-";

        for (Path file : FileUtils.list(modsDirectory)) {
            final String fileName = file.getFileName().toString();
            if (fileName.startsWith(fileNamePrefix) && fileName.endsWith(".jar")) {
                Files.delete(file);
            }
        }
    }

    @Override
    public String name() {
        return "install-proxyforward";
    }
}
