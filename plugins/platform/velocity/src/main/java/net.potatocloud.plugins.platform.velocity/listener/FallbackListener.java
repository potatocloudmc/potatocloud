package net.potatocloud.plugins.platform.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;

import java.util.Comparator;
import java.util.Optional;

public final class FallbackListener {

    private final ProxyServer server;
    private final ConnectorAPI api;

    public FallbackListener(ProxyServer server, ConnectorAPI api) {
        this.server = server;
        this.api = api;
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        final Optional<Service> fallback = api.serviceManager().findBestFallback();
        if (fallback.isEmpty()) {
            return;
        }

        server.getServer(fallback.get().name()).ifPresent(event::setInitialServer);
    }

    @Subscribe
    public void onKicked(KickedFromServerEvent event) {
        final String kickedServiceName = event.getServer().getServerInfo().getName();
        final Optional<Service> fallback = api.serviceManager().fallbackServices().stream()
                .filter(service -> !service.name().equalsIgnoreCase(kickedServiceName))
                .filter(Service::running)
                .filter(service -> !service.full())
                .min(Comparator.comparingInt(Service::playerCount));

        if (fallback.isEmpty()) {
            return;
        }

        server.getServer(fallback.get().name())
                .ifPresent(fallbackServer -> event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallbackServer)));
    }
}
