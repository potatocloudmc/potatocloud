package net.potatocloud.plugins.platform.spigot;

import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpigotPlugin extends JavaPlugin implements Listener, PlatformPlugin {

    private ConnectorAPI api;
    private Service currentService;

    @Override
    public void onLoad() {
        api = new ConnectorAPI();
    }

    @Override
    public void onEnable() {
        initCurrentService();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onServiceReady(Service service) {
        currentService = service;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (currentService == null) {
            return;
        }
        event.setMaxPlayers(currentService.maxPlayers());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (currentService == null) {
            return;
        }
        if (getServer().getOnlinePlayers().size() < currentService.maxPlayers()) {
            return;
        }
        if (event.getPlayer().hasPermission("potatocloud.maxplayers.bypass")) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_FULL, "§cThe server has reached its maximum players!");
    }

    @Override
    public void onDisable() {
        api.shutdown();
    }
}
