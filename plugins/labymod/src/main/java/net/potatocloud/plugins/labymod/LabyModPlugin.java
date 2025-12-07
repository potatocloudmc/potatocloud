package net.potatocloud.plugins.labymod;

import net.labymod.serverapi.server.bukkit.LabyModProtocolService;
import net.potatocloud.plugins.labymod.listener.LabyModPlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LabyModPlugin extends JavaPlugin {

    private Config config;

    @Override
    public void onEnable() {
        this.config = new Config();
        this.config.load();
        LabyModProtocolService.initialize(this);

        Bukkit.getPluginManager().registerEvents(new LabyModPlayerJoinListener(config), this);
    }
}
