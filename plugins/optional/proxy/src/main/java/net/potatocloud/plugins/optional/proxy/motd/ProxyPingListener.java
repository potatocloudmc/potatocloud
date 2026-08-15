package net.potatocloud.plugins.optional.proxy.motd;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.config.Config;
import net.potatocloud.plugins.optional.proxy.ProxyPlugin;
import net.potatocloud.plugins.shared.MessageUtils;

import java.util.Optional;

@RequiredArgsConstructor
public class ProxyPingListener {

    private final ProxyPlugin plugin;
    private final Config config;

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        event.setPing(serverPing(event.getPing()));
    }

    private ServerPing serverPing(ServerPing ping) {
        final int onlinePlayers = CloudAPI.instance().playerManager().players().size();

        final Optional<Service> service = CloudAPI.instance().serviceManager().current();
        if (service.isEmpty()) {
            return ping;
        }

        final int maxPlayers = service.get().maxPlayers();

        final Motd motd = plugin.isMaintenance() ? maintenanceMotd() : defaultMotd();

        if (motd.version() == null) {
            return ping.asBuilder()
                    .onlinePlayers(onlinePlayers)
                    .maximumPlayers(maxPlayers)
                    .description(MessageUtils.format(motd.firstLine())
                            .append(Component.text("\n"))
                            .append(MessageUtils.format(motd.secondLine())))
                    .build();
        }

        return ping.asBuilder()
                .onlinePlayers(onlinePlayers)
                .maximumPlayers(maxPlayers)
                .description(MessageUtils.format(motd.firstLine())
                        .append(Component.text("\n"))
                        .append(MessageUtils.format(motd.secondLine())))
                .version(new ServerPing.Version(-1, LegacyComponentSerializer.legacySection().serialize(MessageUtils.format(motd.version()))))
                .build();
    }

    private Motd defaultMotd() {
        return new Motd(config.get("motd.default.firstLine").asString(), config.get("motd.default.secondLine").asString());
    }

    private Motd maintenanceMotd() {
        return new Motd(config.get("motd.maintenance.firstLine").asString(),
                config.get("motd.maintenance.secondLine").asString(),
                config.get("motd.maintenance.version").asString());
    }
}
