package net.potatocloud.plugins.optional.cloudcommand;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.optional.cloudcommand.command.CloudCommand;
import net.potatocloud.plugins.shared.MessagesConfig;

import java.nio.file.Path;
import java.util.logging.Logger;

public class CloudCommandPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Config config;
    private final MessagesConfig messagesConfig;

    @Inject
    public CloudCommandPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        final String folder = "plugins/potatocloud-cloudcommand";

        config = new YamlConfig(Path.of(folder).resolve("config.yml"));
        messagesConfig = new MessagesConfig(folder);

        config.load();
        messagesConfig.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getCommandManager().register(server.getCommandManager().metaBuilder("cloud")
                .aliases(commandAliases()).build(), new CloudCommand(config, messagesConfig));
    }

    private String[] commandAliases() {
        return config.get("aliases").asStringList().toArray(new String[0]);
    }
}

