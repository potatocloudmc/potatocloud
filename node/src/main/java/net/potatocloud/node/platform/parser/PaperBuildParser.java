package net.potatocloud.node.platform.parser;

import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;
import net.potatocloud.node.version.VersionUtil;
import tools.jackson.databind.JsonNode;

public final class PaperBuildParser implements BuildParser {

    private static final String API = "https://fill.papermc.io/v3/projects/";

    private final String projectName;

    public PaperBuildParser(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String versionName = version.name();

            if (versionName.equalsIgnoreCase("latest")) {
                final JsonNode versions = RequestUtil.request(API + projectName).get("versions");

                versionName = versions.propertyNames().stream()
                        .max(VersionUtil::compare)
                        .flatMap(group -> versions.get(group).asArray().values().stream()
                                .map(JsonNode::asString)
                                .filter(this::hasBuilds)
                                .findFirst())
                        .orElseThrow(() -> new IllegalStateException("No versions with builds found"));
            }

            final JsonNode build = RequestUtil.request(API + projectName + "/versions/" + versionName + "/builds/latest");

            final JsonNode downloads = build.get("downloads");
            final JsonNode download = downloads != null ? downloads.get("server:default") : null;

            if (download == null) {
                throw new IllegalStateException("Missing server download.");
            }

            final String sha256 = download.get("checksums").get("sha256").asString();
            final int buildId = build.get("id").asInt();

            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", String.valueOf(buildId))
                    .replace("{sha256}", sha256);

            if (version instanceof PlatformVersionImpl platformVersion) {
                platformVersion.fileHash(sha256);
                platformVersion.downloadUrl(downloadUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Paper build for: " + projectName + " : " + version.name(), e);
        }
    }

    private boolean hasBuilds(String version) {
        final JsonNode builds = RequestUtil.request(API + projectName + "/versions/" + version + "/builds/latest");
        return builds != null && builds.get("id") != null;
    }

    @Override
    public String getName() {
        return projectName;
    }
}