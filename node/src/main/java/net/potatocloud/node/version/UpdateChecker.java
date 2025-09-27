package net.potatocloud.node.version;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.utils.RequestUtil;
import net.potatocloud.node.Node;

public class UpdateChecker {

    private static final String REPO_OWNER = "potatocloudmc";
    private static final String REPO_NAME = "potatocloud";

    @SneakyThrows
    public boolean isUpdateAvailable() {
        Node.getInstance().getLogger().info("Checking for updates&8...");
        return !getLatestVersion().equals(CloudAPI.VERSION);
    }

    @SneakyThrows
    public String getLatestVersion() {
        final String url = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";
        final JsonObject response = RequestUtil.request(url);
        return response.get("tag_name").getAsString();
    }
}
