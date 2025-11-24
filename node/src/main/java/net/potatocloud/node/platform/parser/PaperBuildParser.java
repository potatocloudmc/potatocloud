package net.potatocloud.node.platform.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.api.utils.RequestUtil;
import net.potatocloud.node.platform.BuildParser;

@RequiredArgsConstructor
public class PaperBuildParser implements BuildParser {

    private final String projectName;

    @Override
    public String getName() {
        return projectName;
    }

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String mcVersion = version.getName();

            // Find the latest minecraft version if the user wants the latest
            if (mcVersion.equalsIgnoreCase("latest")) {
                final JsonArray versions = RequestUtil.request("https://api.papermc.io/v2/projects/" + projectName).getAsJsonArray("versions");

                mcVersion = versions.get(versions.size() - 1).getAsString();
            }

            // Get the latest build of the chosen version
            final JsonArray builds = RequestUtil.request("https://api.papermc.io/v2/projects/" + projectName + "/versions/" + mcVersion).getAsJsonArray("builds");
            final int latestBuild = builds.get(builds.size() - 1).getAsInt();

            final JsonObject buildJson = RequestUtil.request(
                    "https://api.papermc.io/v2/projects/" + projectName +
                            "/versions/" + mcVersion +
                            "/builds/" + latestBuild
            );

            final JsonObject application = buildJson.getAsJsonObject("downloads").getAsJsonObject("application");
            final String sha256 = application.get("sha256").getAsString();

            // Replace placeholders in the platform download URL
            final String downloadUrl = baseUrl
                    .replace("{version}", mcVersion)
                    .replace("{build}", String.valueOf(latestBuild))
                    .replace("{sha256}", sha256);

            if (version instanceof PlatformVersionImpl impl) {
                impl.setFileHash(sha256);
                impl.setDownloadUrl(downloadUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
