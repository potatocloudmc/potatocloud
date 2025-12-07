package net.potatocloud.plugins.proxy.motd;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.plugins.proxy.Config;

@RequiredArgsConstructor
public class ProxyPingListener {

    private final Config config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Subscribe
    public void handle(ProxyPingEvent event) {
        event.setPing(this.serverPing(event.getPing()));
    }

    private ServerPing serverPing(ServerPing ping) {
        final int onlinePlayers = CloudAPI.getInstance().getPlayerManager().getOnlinePlayers().size();
        final int maxPlayers = CloudAPI.getInstance().getServiceManager().getCurrentService().getMaxPlayers();
        final Motd motd = config.maintenance() ? config.maintenanceMotd() : config.defaultMotd();

        if (motd.version() == null) {
            return ping.asBuilder()
                    .onlinePlayers(onlinePlayers)
                    .maximumPlayers(maxPlayers)
                    .description(this.from(motd.firstLine())
                            .append(Component.text("\n"))
                            .append(this.from(motd.secondLine())))
                    .build();
        }

        return ping.asBuilder()
                .onlinePlayers(onlinePlayers)
                .maximumPlayers(maxPlayers)
                .description(this.from(motd.firstLine())
                        .append(Component.text("\n"))
                        .append(this.from(motd.secondLine())))
                .version(new ServerPing.Version(-1, LegacyComponentSerializer.legacySection().serialize(this.from(motd.version()))))
                .build();

    }

    private Component from(String string) {
        return miniMessage.deserialize(string);
    }
}
