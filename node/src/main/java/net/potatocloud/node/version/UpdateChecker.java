package net.potatocloud.node.version;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.version.Version;
import net.potatocloud.node.utils.GitHubClient;
import tools.jackson.databind.JsonNode;

public final class UpdateChecker {

    private static final String REPOSITORY = "potatocloudmc/potatocloud";

    private final Logger logger;

    public UpdateChecker(Logger logger) {
        this.logger = logger;
    }

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
        try {
            final JsonNode response = GitHubClient.latestRelease(REPOSITORY);

            if (response == null || !response.has("tag_name")) {
                return null;
            }

            return Version.fromString(response.get("tag_name").stringValue());
        } catch (Exception exception) {
            logger.error("Failed to check for updates. Github API might be down or rate limited. Error: " + exception.getMessage());
            return null;
        }
    }
}
