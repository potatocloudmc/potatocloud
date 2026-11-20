package net.potatocloud.plugins.platform.limbo.listener;

import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerLoginEvent;
import net.kyori.adventure.text.Component;
import net.potatocloud.connector.utils.ServiceConnector;

public final class MaintenanceListener implements Listener {

    private final ServiceConnector connector;

    public MaintenanceListener(ServiceConnector connector) {
        this.connector = connector;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        connector.currentService()
                .filter(service -> service.group().maintenance())
                .filter(_ -> !event.getConnection().getPlayer().hasPermission(ServiceConnector.MAINTENANCE_BYPASS_PERMISSION))
                .ifPresent(_ -> {
                    event.setCancelReason(Component.text("§c" + ServiceConnector.MAINTENANCE_MESSAGE));
                    event.setCancelled(true);
                });
    }
}
