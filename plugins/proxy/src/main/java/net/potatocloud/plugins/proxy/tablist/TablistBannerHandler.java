package net.potatocloud.plugins.proxy.tablist;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.RequiredArgsConstructor;
import net.labymod.serverapi.server.velocity.LabyModPlayer;
import net.labymod.serverapi.server.velocity.event.LabyModPlayerJoinEvent;
import net.potatocloud.plugins.proxy.Config;

import java.util.Optional;

@RequiredArgsConstructor
public class TablistBannerHandler {

    private final Config config;

    @Subscribe
    public void labyModPlayerConnect(LabyModPlayerJoinEvent event) {
        final LabyModPlayer labyModPlayer = event.labyModPlayer();
        final Player player = labyModPlayer.getPlayer();
        final Optional<ServerConnection> serverConnection = player.getCurrentServer();

        if (serverConnection.isPresent())
            labyModPlayer.sendTabListBanner(this.config.getTablistBannerUrl());
    }
}
