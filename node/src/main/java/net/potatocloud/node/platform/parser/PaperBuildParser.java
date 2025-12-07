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

            final JsonObject project = RequestUtil.request("https://fill.papermc.io/v3/projects/" + projectName);
            final JsonObject versions = project.getAsJsonObject("versions");

            // Find the latest minecraft version if the user wants the latest
            if (versionName.equalsIgnoreCase("latest")) {
                final List<String> allVersions = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : versions.entrySet()) {
                    final JsonArray versionsArray = entry.getValue().getAsJsonArray();

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
            final JsonObject serverDefault = downloads.getAsJsonObject("server:default");
            final String sha256 = serverDefault
                    .getAsJsonObject("checksums")
                    .get("sha256")
                    .getAsString();

            // Replace placeholders in the platform download URL
            final String downloadUrl = baseUrl
                    .replace("{version}", versionName)
                    .replace("{build}", String.valueOf(latestBuildId))
                    .replace("{sha256}", sha256);

            if (version instanceof PlatformVersionImpl impl) {
                impl.setFileHash(sha256);
                impl.setDownloadUrl(downloadUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return projectName;
    }
}
