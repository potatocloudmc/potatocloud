package net.potatocloud.node.platform.parser;

import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;
import tools.jackson.databind.JsonNode;

public final class PurpurBuildParser implements BuildParser {

    private static final String API = "https://api.purpurmc.org/v2/purpur/";

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String versionName = version.name();

            if (versionName.equalsIgnoreCase("latest")) {
                final JsonNode project = RequestUtil.request(API);

                versionName = project
                        .get("metadata")
                        .get("current")
                        .asString();
            }

            final JsonNode versionInfo = RequestUtil.request(API + versionName);
            final JsonNode builds = versionInfo.get("builds");

            if (builds == null || builds.isEmpty()) {
                throw new RuntimeException("No builds found for version: " + versionName);
            }

            final String build = builds.get("latest").asString();

            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", build);

            final JsonNode latestBuild = RequestUtil.request(API + versionName + "/" + build);

            final String md5 = latestBuild.get("md5").asString();

            if (version instanceof PlatformVersionImpl platformVersion) {
                platformVersion.fileHash(md5);
                platformVersion.downloadUrl(downloadUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Purpur build for: " + version.name(), e);
        }
    }

    @Override
    public String getName() {
        return "purpur";
    }
}
