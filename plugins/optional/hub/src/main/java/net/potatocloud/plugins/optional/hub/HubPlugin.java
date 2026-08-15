package net.potatocloud.plugins.optional.hub;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.optional.hub.commands.HubCommand;
import net.potatocloud.plugins.shared.MessagesConfig;


import java.nio.file.Path;
import java.util.logging.Logger;

public class HubPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Config config;
    private final MessagesConfig messagesConfig;

    @Inject
    public HubPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        final String folder = "plugins/potatocloud-hub";

        config = new YamlConfig(Path.of(folder, "config.yml"));
        messagesConfig = new MessagesConfig(folder);
        config.load();
        messagesConfig.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getCommandManager().register(server.getCommandManager().metaBuilder("hub")
                .aliases(commandAliases()).build(), new HubCommand(messagesConfig, server));
    }

    private String[] commandAliases() {
        return config.get("aliases").asStringList().toArray(new String[0]);
    }
}
