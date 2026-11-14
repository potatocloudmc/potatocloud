package net.potatocloud.module.rest;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.module.rest.config.RestConfig;

public final class RestModule extends AbstractModule {

    private RestServer server;

    @Override
    public void onEnable() {
        final Logger logger = CloudAPI.instance().logger();

        try {
            final RestConfig config = RestConfig.load(dataFolder().resolve("config.yml"));

            server = new RestServer(CloudAPI.instance(), config);
            server.start();

            logger.info("REST API listening on &a" + config.host() + "&8:&a" + config.port());
        } catch (Exception e) {
            server = null;
            logger.error("Failed to start REST API: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
