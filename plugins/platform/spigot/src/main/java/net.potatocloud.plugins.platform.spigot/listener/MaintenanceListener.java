package net.potatocloud.plugins.platform.spigot.listener;

import net.potatocloud.connector.utils.ServiceConnector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class MaintenanceListener implements Listener {

    private final ServiceConnector connector;

    public MaintenanceListener(ServiceConnector connector) {
        this.connector = connector;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        connector.currentService()
                .filter(service -> service.group().maintenance())
                .filter(_ -> !event.getPlayer().hasPermission(ServiceConnector.MAINTENANCE_BYPASS_PERMISSION))
                .ifPresent(_ -> event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "§c" + ServiceConnector.MAINTENANCE_MESSAGE));
    }
}
