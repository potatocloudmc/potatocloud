package net.potatocloud.plugins.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.labymod.serverapi.server.velocity.LabyModProtocolService;
import net.potatocloud.plugins.proxy.commands.ProxyCommand;
import net.potatocloud.plugins.proxy.maintenance.LoginListener;
import net.potatocloud.plugins.proxy.motd.ProxyPingListener;
import net.potatocloud.plugins.proxy.tablist.TablistBannerHandler;
import net.potatocloud.plugins.proxy.tablist.TablistHandler;
import org.slf4j.Logger;

public class ProxyPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final MessagesConfig messagesConfig;
    private final Config config;

    @Inject
    public ProxyPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.messagesConfig = new MessagesConfig();
        this.messagesConfig.load();
        this.config = new Config();
        this.config.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        LabyModProtocolService.initialize(this, this.server, logger);

        final EventManager eventManager = this.server.getEventManager();
        if (this.config.useMotd()) {
            eventManager.register(this, new ProxyPingListener(this.config));
        }
        if (this.config.useTablist()) {
            eventManager.register(this, new TablistHandler(this.config, this.server));
        }
        if (this.config.useTablistBanner()) {
            eventManager.register(this, new TablistBannerHandler(this.config));
        }
        eventManager.register(this, new LoginListener(this.config, this.messagesConfig));

        this.server.getCommandManager().register(server.getCommandManager().metaBuilder("proxy").build(), new ProxyCommand(this.config, this.messagesConfig));

    }
}
