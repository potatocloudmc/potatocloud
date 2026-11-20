package net.potatocloud.plugins.platform.limbo.listener;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerLoginEvent;
import com.loohp.limbo.events.status.StatusPingEvent;
import net.kyori.adventure.text.Component;
import net.potatocloud.connector.utils.ServiceConnector;

public final class MaxPlayersListener implements Listener {

    private final ServiceConnector connector;

    public MaxPlayersListener(ServiceConnector connector) {
        this.connector = connector;
    }

    @EventHandler
    public void onStatusPing(StatusPingEvent event) {
        connector.currentService().ifPresent(service -> event.setMaxPlayers(service.maxPlayers()));
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        connector.currentService()
                .filter(service -> Limbo.getInstance().getPlayers().size() >= service.maxPlayers())
                .filter(_ -> !event.getConnection().getPlayer().hasPermission(ServiceConnector.MAX_PLAYERS_BYPASS_PERMISSION))
                .ifPresent(_ -> {
                    event.setCancelReason(Component.text("§c" + ServiceConnector.SERVER_FULL_MESSAGE));
                    event.setCancelled(true);
                });
    }
}
