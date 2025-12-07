package net.potatocloud.plugins.hub;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.potatocloud.plugins.hub.commands.HubCommand;

import java.util.logging.Logger;

public class HubPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final MessagesConfig messagesConfig;
    private final Config config;

    @Inject
    public HubPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.messagesConfig = new MessagesConfig();
        this.messagesConfig.load();
        this.config = new Config();
        this.config.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getCommandManager().register(server.getCommandManager().metaBuilder("hub").aliases(this.config.aliases()).build(), new HubCommand(this.messagesConfig, this.server));
    }
}
