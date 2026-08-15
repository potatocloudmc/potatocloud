package net.potatocloud.plugins.optional.proxy.tablist;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.shared.MessageUtils;

import java.util.Optional;

@RequiredArgsConstructor
public class TablistHandler {

    private final Config config;
    private final ProxyServer server;

    @Subscribe
    public void onServerPostConnection(ServerPostConnectEvent event) {
        server.getAllPlayers().forEach(this::update);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        server.getAllPlayers().forEach(this::update);
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        this.update(event.getPlayer());
    }

    private void update(Player player) {
        final CloudPlayer cloudPlayer = CloudAPI.instance().playerManager().find(player.getUsername()).orElse(null);
        if (cloudPlayer == null) {
            return;
        }

        final Optional<Service> optionalService = cloudPlayer.service();

        if (optionalService.isEmpty() || optionalService.get().group() == null) {
            return;
        }

        final Service service = optionalService.get();
        final String group = service.group().name();
        final String proxy = cloudPlayer.proxy().name();

        final int onlinePlayers = CloudAPI.instance()
                .playerManager()
                .players()
                .size();

        final int maxPlayers = CloudAPI.instance()
                .serviceManager()
                .current()
                .map(Service::maxPlayers)
                .orElse(0);

        final Tablist tablist = new Tablist(
                config.get("tablist.header").asString(),
                config.get("tablist.footer").asString()
        );

        final Component header = replacePlaceholders(
                tablist.header(),
                service.name(),
                group,
                proxy,
                onlinePlayers,
                maxPlayers
        );

        final Component footer = replacePlaceholders(
                tablist.footer(),
                service.name(),
                group,
                proxy,
                onlinePlayers,
                maxPlayers
        );

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private Component replacePlaceholders(String text, String service, String group, String proxy, int onlinePlayers, int maxPlayers) {
        return MessageUtils.format(text)
                .replaceText(b -> b.match("%service%").replacement(service))
                .replaceText(b -> b.match("%group%").replacement(group))
                .replaceText(b -> b.match("%proxy%").replacement(proxy))
                .replaceText(b -> b.match("%online_players%").replacement(String.valueOf(onlinePlayers)))
                .replaceText(b -> b.match("%max_players%").replacement(String.valueOf(maxPlayers)));
    }
}
