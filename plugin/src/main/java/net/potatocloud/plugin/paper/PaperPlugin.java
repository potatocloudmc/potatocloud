package net.potatocloud.plugin.paper;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugin.PlatformPlugin;
import net.potatocloud.plugin.api.impl.PluginCloudAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperPlugin extends JavaPlugin implements Listener, PlatformPlugin {

    private PluginCloudAPI api;
    private Service currentService;

    @Override
    public void onLoad() {
        api = new PluginCloudAPI();
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
        event.setMaxPlayers(currentService.getMaxPlayers());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (currentService == null) {
            return;
        }
        if (getServer().getOnlinePlayers().size() < currentService.getMaxPlayers()) {
            return;
        }
        if (event.getPlayer().hasPermission("potatocloud.maxplayers.bypass")) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_FULL, MiniMessage.miniMessage().deserialize("<red>The server has reached its maximum players!"));
    }

    @Override
    public void runTaskLater(Runnable task, int delaySeconds) {
        getServer().getScheduler().runTaskLater(this, task, delaySeconds * 20L);
    }

    @Override
    public void onDisable() {
        api.shutdown();
    }
}
