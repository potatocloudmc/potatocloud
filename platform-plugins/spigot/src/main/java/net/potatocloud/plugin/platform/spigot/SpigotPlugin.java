package net.potatocloud.plugin.platform.spigot;

import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPlugin extends JavaPlugin implements Listener, PlatformPlugin {

    private ConnectorAPI api;
    private Service currentService;

    public ConnectorAPI getApi() {
        return api;
    }

    private static SpigotPlugin instance;

    public static SpigotPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        api = new ConnectorAPI();
    }

    @Override
    public void onEnable() {
        instance = this;
        initCurrentService();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onServiceReady(Service service) {
        currentService = service;

        CoreAPI.getInstance().requestTranslations("debug");
        CoreAPI.getInstance().requestTranslations("group");
        CoreAPI.getInstance().getHookManager().getHooks().forEach(iCloudHook -> {
            iCloudHook.example();
        });
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
        event.disallow(PlayerLoginEvent.Result.KICK_FULL, "§cThe server has reached its maximum players!");
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
