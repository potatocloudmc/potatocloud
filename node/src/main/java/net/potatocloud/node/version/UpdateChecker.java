package net.potatocloud.node.version;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.utils.version.Version;
import net.potatocloud.node.utils.RequestUtil;

@RequiredArgsConstructor
public class UpdateChecker {

    private static final String REPO_OWNER = "potatocloudmc";
    private static final String REPO_NAME = "potatocloud";

    private final Logger logger;

    public void checkForUpdates() {
        logger.info("Checking for updates&8...");

        final Version latest = getLatestVersion();
        if (latest == null) {
            logger.info("&cFailed check for updates&8...");
            return;
        }

        if (CloudAPI.VERSION.equals(latest)) {
            logger.info("You are running the latest version&8!");
            return;
        }

        if (CloudAPI.VERSION.compareTo(latest) > 0) {
            logger.warn("You are running a newer version than the latest release! &8(&7Latest&8: &a"
                    + latest + "&8, &7Current&8: &a" + CloudAPI.VERSION + "&8)");
            return;
        }

        logger.warn("A new version is available! &8(&7Latest&8: &a" + latest + "&8, &7Current&8: &a" + CloudAPI.VERSION + "&8)");
    }

    public Version getLatestVersion() {
        final String url = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";
        final JsonObject response = RequestUtil.request(url);

        if (response == null || !response.has("tag_name")) {
            return null;
        }

        return Version.fromString(response.get("tag_name").getAsString());
    }
}
