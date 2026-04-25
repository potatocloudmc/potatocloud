package net.potatocloud.node.platform.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PaperBuildParser implements BuildParser {

    private final String projectName;

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String versionName = version.getName();

            // Find the latest Minecraft version if the user wants the latest
            if (versionName.equalsIgnoreCase("latest")) {
                final JsonObject project = RequestUtil.request("https://fill.papermc.io/v3/projects/" + projectName);
                final JsonObject versions = project.getAsJsonObject("versions");

                final List<String> allVersions = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : versions.entrySet()) {
                    final JsonArray versionsArray = entry.getValue().getAsJsonArray();

                    if (versionsArray == null || versionsArray.isEmpty()) {
                        throw new RuntimeException("No versions found in Paper API");
                    }

                    for (JsonElement element : versionsArray) {
                        allVersions.add(element.getAsString());
                    }
                }

                versionName = allVersions.getFirst();
            }

            // Get the latest build of the chosen version
            final JsonObject latestBuild = RequestUtil.request("https://fill.papermc.io/v3/projects/"
                    + projectName + "/versions/" + versionName + "/builds/latest");

            final int latestBuildId = latestBuild.get("id").getAsInt();

            final JsonObject downloads = latestBuild.getAsJsonObject("downloads");
            final JsonObject serverDefault = downloads != null ? downloads.getAsJsonObject("server:default") : null;

            if (serverDefault == null) {
                throw new RuntimeException("Missing download info for Paper build");
            }

            final String sha256 = serverDefault
                    .getAsJsonObject("checksums")
                    .get("sha256")
                    .getAsString();

            // Replace placeholders in the platform download URL
            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", String.valueOf(latestBuildId))
                    .replace("{sha256}", sha256);

            if (version instanceof PlatformVersionImpl versionImpl) {
                versionImpl.setFileHash(sha256);
                versionImpl.setDownloadUrl(downloadUrl);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Paper build for: " + projectName + " : " + version.getName(), e);
        }
    }

    @Override
    public String getName() {
        return projectName;
    }
}
