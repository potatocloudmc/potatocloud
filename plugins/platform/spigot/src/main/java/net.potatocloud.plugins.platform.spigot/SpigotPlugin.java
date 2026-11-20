package net.potatocloud.plugins.platform.spigot;

import net.potatocloud.connector.utils.ServiceConnector;
import net.potatocloud.plugins.platform.spigot.listener.MaintenanceListener;
import net.potatocloud.plugins.platform.spigot.listener.MaxPlayersListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpigotPlugin extends JavaPlugin {

    private ServiceConnector connector;

    @Override
    public void onLoad() {
        connector = new ServiceConnector();
    }

    @Override
    public void onEnable() {
        connector.initCurrentService();
        getServer().getPluginManager().registerEvents(new MaxPlayersListener(connector), this);
        getServer().getPluginManager().registerEvents(new MaintenanceListener(connector), this);
    }

    @Override
    public void onDisable() {
        connector.shutdown();
    }
}
