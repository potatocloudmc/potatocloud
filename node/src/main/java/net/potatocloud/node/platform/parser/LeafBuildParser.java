package net.potatocloud.node.platform.parser;

import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;
import net.potatocloud.node.version.VersionUtil;
import tools.jackson.databind.JsonNode;

public final class LeafBuildParser implements BuildParser {

    public static final String API = "https://api.leafmc.one/v2/projects/leaf";

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String versionName = version.name();

            if (versionName.equalsIgnoreCase("latest")) {
                final JsonNode versions = RequestUtil.request(API).get("versions");

                versionName = versions.values().stream()
                        .map(JsonNode::asString)
                        .filter(this::hasBuilds)
                        .max(VersionUtil::compare)
                        .orElseThrow(() -> new IllegalStateException("No versions with builds found"));
            }

            final JsonNode versionInfo = RequestUtil.request(API + "/versions/" + versionName);
            final JsonNode builds = versionInfo.get("builds");

            if (builds == null || builds.isEmpty()) {
                throw new RuntimeException("No builds found for version: " + versionName);
            }

            final int latestBuildId = builds.get(builds.size() - 1).asInt();

            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", String.valueOf(latestBuildId));

            final JsonNode latestBuild = RequestUtil.request(API + "/versions/" + versionName + "/builds/" + latestBuildId);

            final JsonNode downloads = latestBuild.get("downloads");
            final JsonNode primary = downloads != null ? downloads.get("primary") : null;

            if (primary == null) {
                throw new RuntimeException("Missing download info");
            }

            if (version instanceof PlatformVersionImpl platformVersion) {
                platformVersion.fileHash(primary.get("sha256").asString());
                platformVersion.downloadUrl(downloadUrl);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Leaf build for: " + version.name(), e);
        }
    }

    private boolean hasBuilds(String version) {
        final JsonNode versionJson = RequestUtil.request(API + "/versions/" + version);
        return versionJson != null && versionJson.get("builds") != null && !versionJson.get("builds").isEmpty();
    }

    @Override
    public String getName() {
        return "leaf";
    }
}