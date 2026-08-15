package net.potatocloud.plugins.optional.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.labymod.serverapi.server.velocity.LabyModProtocolService;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.optional.proxy.commands.ProxyCommand;
import net.potatocloud.plugins.optional.proxy.maintenance.LoginListener;
import net.potatocloud.plugins.optional.proxy.motd.ProxyPingListener;
import net.potatocloud.plugins.optional.proxy.tablist.TablistBannerHandler;
import net.potatocloud.plugins.optional.proxy.tablist.TablistHandler;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class ProxyPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final MessagesConfig messagesConfig;
    private final Config config;

    @Inject
    public ProxyPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        final String folder = "plugins/potatocloud-proxy";

        config = new YamlConfig(Path.of(folder).resolve("config.yml"));
        messagesConfig = new MessagesConfig(folder);
        config.load();
        messagesConfig.load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        LabyModProtocolService.initialize(this, server, logger);

        final EventManager eventManager = server.getEventManager();

        if (config.get("useMotd").asBoolean()) {
            eventManager.register(this, new ProxyPingListener(this, config));
        }

        if (config.get("useTablist").asBoolean()) {
            eventManager.register(this, new TablistHandler(config, server));
        }
        if (config.get("useTablistBanner").asBoolean()) {
            eventManager.register(this, new TablistBannerHandler(config));
        }

        eventManager.register(this, new LoginListener(this, config, messagesConfig));

        server.getCommandManager().register(server.getCommandManager().metaBuilder("proxy")
                .aliases(this.commandAliases()).build(), new ProxyCommand(this, config, messagesConfig));
    }

    private String[] commandAliases() {
        return config.get("command-aliases").asStringList().toArray(new String[0]);
    }

    public boolean isMaintenance() {
        return config.get("maintenance").asBoolean();
    }

    public List<String> getWhitelist() {
        return config.get("whitelist").asStringList();
    }

    public void setWhitelist(List<String> whitelist) {
        config.set("whitelist", whitelist);
        config.save();
    }

    public void setMaintenance(boolean maintenance) {
        config.set("maintenance", maintenance);
        config.save();
    }
}
