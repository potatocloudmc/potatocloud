package net.potatocloud.node.platform.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;

public class PurpurBuildParser implements BuildParser {

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String versionName = version.getName();

            // Find the latest Minecraft version if the user wants the latest
            if (versionName.equalsIgnoreCase("latest")) {
                final JsonObject project = RequestUtil.request("https://api.purpurmc.org/v2/purpur/");

                final JsonArray versionsArray = project.getAsJsonArray("versions");
                if (versionsArray == null || versionsArray.isEmpty()) {
                    throw new RuntimeException("No versions found in Purpur API");
                }

                versionName = versionsArray
                        .get(versionsArray.size() - 1)
                        .getAsString();
            }

            // Get version info
            final JsonObject versionInfo = RequestUtil.request("https://api.purpurmc.org/v2/purpur/" + versionName);
            final JsonObject buildsObject = versionInfo.getAsJsonObject("builds");

            if (buildsObject == null || buildsObject.isEmpty()) {
                throw new RuntimeException("No builds found for version: " + versionName);
            }

            // Get the latest build of the chosen version
            final String latestBuildName = buildsObject.get("latest").getAsString();

            // Replace placeholders in the platform download URL
            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", latestBuildName);

            final JsonObject latestBuild = RequestUtil
                    .request("https://api.purpurmc.org/v2/purpur/" + versionName + "/" + latestBuildName);

            final String md5 = latestBuild.get("md5").getAsString();

            if (version instanceof PlatformVersionImpl versionImpl) {
                versionImpl.setFileHash(md5);
                versionImpl.setDownloadUrl(downloadUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Purpur build for: " + version.getName(), e);
        }
    }

    @Override
    public String getName() {
        return "purpur";
    }
}
