package net.potatocloud.plugin.server.signs;

import net.potatocloud.plugin.server.shared.Config;
import net.potatocloud.plugin.server.shared.MessagesConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class SignPlugin extends JavaPlugin {

    private Config config;
    private MessagesConfig messagesConfig;
    private Config signConfig;
    private Config layoutConfig;

    @Override
    public void onEnable() {
        final String folder = "plugins/potatocloud-signs";

        this.config = new Config(folder, "config.yml");
        this.config.load();

        this.messagesConfig = new MessagesConfig(folder);
        this.messagesConfig.load();

        this.signConfig = new Config(folder, "signs.yml");
        this.signConfig.load();

        this.layoutConfig = new Config(folder, "layout.yml");
        this.layoutConfig.load();
    }
}
