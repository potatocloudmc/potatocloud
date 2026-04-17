package net.potatocloud.node.platform.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;

public class LeafBuildParser implements BuildParser {

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String versionName = version.getName();

            // Find the latest Minecraft version if the user wants the latest
            if (versionName.equalsIgnoreCase("latest")) {
                final JsonObject project = RequestUtil.request("https://api.leafmc.one/v2/projects/leaf");

                final JsonArray versionsArray = project.getAsJsonArray("versions");

                if (versionsArray == null || versionsArray.isEmpty()) {
                    throw new RuntimeException("No versions found in Leaf API");
                }

                versionName = versionsArray
                        .get(0)
                        .getAsString();
            }

            // Get version info
            final JsonObject versionInfo = RequestUtil.request("https://api.leafmc.one/v2/projects/leaf/versions/" + versionName);
            final JsonArray buildsArray = versionInfo.getAsJsonArray("builds");

            if (buildsArray == null || buildsArray.isEmpty()) {
                throw new RuntimeException("No builds found for version: " + versionName);
            }

            // Get the latest build of the chosen version
            final int latestBuildId = buildsArray.get(buildsArray.size() - 1).getAsInt();

            // Replace placeholders in the platform download URL
            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", String.valueOf(latestBuildId));

            final JsonObject latestBuild = RequestUtil
                    .request("https://api.leafmc.one/v2/projects/leaf/versions/" + versionName + "/builds/" + latestBuildId);

            final JsonObject downloads = latestBuild.getAsJsonObject("downloads");
            final JsonObject primary = downloads != null ? downloads.getAsJsonObject("primary") : null;

            if (primary == null) {
                throw new RuntimeException("Missing download info for Leaf build");
            }

            if (version instanceof PlatformVersionImpl versionImpl) {
                versionImpl.setFileHash(primary.get("sha256").getAsString());
                versionImpl.setDownloadUrl(downloadUrl);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Leaf build for: " + version.getName(), e);
        }
    }

    @Override
    public String getName() {
        return "leaf";
    }
}