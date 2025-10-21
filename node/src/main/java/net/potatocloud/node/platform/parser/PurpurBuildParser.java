package net.potatocloud.node.platform.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.api.utils.RequestUtil;
import net.potatocloud.node.platform.BuildParser;

public class PurpurBuildParser implements BuildParser {

    @Override
    public String getName() {
        return "purpur";
    }

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            String mcVersion = version.getName();

            if (mcVersion.equalsIgnoreCase("latest")) {
                final JsonArray versionsArray = RequestUtil.request("https://api.purpurmc.org/v2/purpur/").getAsJsonArray("versions");

                mcVersion = versionsArray.get(versionsArray.size() - 1).getAsString();
            }

            final JsonObject versionJson = RequestUtil.request("https://api.purpurmc.org/v2/purpur/" + mcVersion);
            final String latestBuild = versionJson.getAsJsonObject("builds").get("latest").getAsString();

            final JsonObject buildJson = RequestUtil.request("https://api.purpurmc.org/v2/purpur/" + mcVersion + "/" + latestBuild);
            final String md5 = buildJson.get("md5").getAsString();

            final String downloadUrl = baseUrl
                    .replace("{version}", mcVersion)
                    .replace("{build}", latestBuild);

            if (version instanceof PlatformVersionImpl impl) {
                impl.setFileHash(md5);
                impl.setDownloadUrl(downloadUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
