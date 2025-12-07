package net.potatocloud.plugins.proxy.tablist;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.proxy.Config;

@RequiredArgsConstructor
public class TablistHandler {

    private final Config config;
    private final ProxyServer proxyServer;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Subscribe
    public void handle(ServerPostConnectEvent event) {
        this.proxyServer.getAllPlayers().forEach(this::updateTablist);
    }

    @Subscribe
    public void handle(DisconnectEvent event) {
        this.proxyServer.getAllPlayers().forEach(this::updateTablist);
    }

    @Subscribe
    public void handle(PlayerChooseInitialServerEvent event) {
        this.updateTablist(event.getPlayer());
    }

    private void updateTablist(Player player) {
        final CloudPlayer cloudPlayer = CloudAPI.getInstance().getPlayerManager().getCloudPlayer(player.getUsername());
        if (cloudPlayer == null) {
            return;
        }

        final String server = cloudPlayer.getConnectedServiceName();
        if (server == null) {
            return;
        }

        final Service service = CloudAPI.getInstance().getServiceManager().getService(server);
        if (service == null || service.getServiceGroup() == null) {
            return;
        }

        final String group = service.getServiceGroup().getName();
        final String proxy = cloudPlayer.getConnectedProxyName();

        final int onlinePlayers = CloudAPI.getInstance().getPlayerManager().getOnlinePlayers().size();
        final int maxPlayers = CloudAPI.getInstance().getServiceManager().getCurrentService().getMaxPlayers();

        final Tablist tablist = this.config.tablist();
        final Component header = this.miniMessage.deserialize(tablist.header())
                .replaceText(text -> text.match("%service%").replacement(server))
                .replaceText(text -> text.match("%group%").replacement(group))
                .replaceText(text -> text.match("%proxy%").replacement(proxy))
                .replaceText(text -> text.match("%online_players%").replacement(String.valueOf(onlinePlayers)))
                .replaceText(text -> text.match("%max_players%").replacement(String.valueOf(maxPlayers)));

        final Component footer = this.miniMessage.deserialize(tablist.footer())
                .replaceText(text -> text.match("%service%").replacement(server))
                .replaceText(text -> text.match("%group%").replacement(group))
                .replaceText(text -> text.match("%proxy%").replacement(proxy))
                .replaceText(text -> text.match("%online_players%").replacement(String.valueOf(onlinePlayers)))
                .replaceText(text -> text.match("%max_players%").replacement(String.valueOf(maxPlayers)));

        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}
