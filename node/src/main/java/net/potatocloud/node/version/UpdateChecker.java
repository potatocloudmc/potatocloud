package net.potatocloud.node.version;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.utils.RequestUtil;
import net.potatocloud.node.console.Logger;

@RequiredArgsConstructor
public class UpdateChecker {

    private static final String REPO_OWNER = "potatocloudmc";
    private static final String REPO_NAME = "potatocloud";

    private final Logger logger;

    public void checkForUpdates() {
        try {
            if (isUpdateAvailable()) {
                logger.warn("A new version is available! &8(&7Latest&8: &a" + getLatestVersion() + "&8, &7Current&8: &a" + CloudAPI.VERSION + "&8)");
                return;
            }
            logger.info("You are running the latest version&8!");
        } catch (Exception e) {
            logger.warn("Failed to check for updates: " + e.getMessage());
        }
    }

    @SneakyThrows
    public boolean isUpdateAvailable() {
        logger.info("Checking for updates&8...");
        return !getLatestVersion().equals(CloudAPI.VERSION);
    }

    @SneakyThrows
    public String getLatestVersion() {
        final String url = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";
        final JsonObject response = RequestUtil.request(url);
        return response.get("tag_name").getAsString();
    }
}
