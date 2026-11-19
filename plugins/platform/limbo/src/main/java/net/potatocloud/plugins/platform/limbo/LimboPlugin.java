package net.potatocloud.plugins.platform.limbo;

import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerLoginEvent;
import com.loohp.limbo.events.status.StatusPingEvent;
import net.kyori.adventure.text.Component;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;

public final class LimboPlugin extends com.loohp.limbo.plugins.LimboPlugin implements Listener, PlatformPlugin {

    private ConnectorAPI api;
    private Service currentService;

    @Override
    public void onLoad() {
        api = new ConnectorAPI();
    }

    @Override
    public void onEnable() {
        initCurrentService();
        getServer().getEventsManager().registerEvents(this, this);
    }

    @Override
    public void onServiceReady(Service service) {
        currentService = service;
    }

    @EventHandler
    public void onStatusPing(StatusPingEvent event) {
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
        if (getServer().getPlayers().size() < currentService.maxPlayers()) {
            return;
        }
        if (event.getConnection().getPlayer().hasPermission("potatocloud.maxplayers.bypass")) {
            return;
        }
        event.setCancelReason(Component.text("§cThe server has reached its maximum players!"));
        event.setCancelled(true);
    }

    @Override
    public void onDisable() {
        api.shutdown();
    }
}
