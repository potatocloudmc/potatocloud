package net.potatocloud.plugins.optional.labymod;

import net.labymod.serverapi.server.bukkit.LabyModProtocolService;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;
import net.potatocloud.plugins.optional.labymod.listener.LabyModPlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class LabyModPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final Config config = new YamlConfig(Path.of("plugins/potatocloud-labymod").resolve("config.yml"));
        config.load();
        LabyModProtocolService.initialize(this);

        getServer().getPluginManager().registerEvents(new LabyModPlayerJoinListener(config), this);
    }
}
