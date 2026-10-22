package net.potatocloud.plugins.optional.npc.connect;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.service.Service;
import net.potatocloud.plugins.optional.npc.util.Placeholders;
import net.potatocloud.plugins.shared.MessagesConfig;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Optional;

public final class ServiceConnector {

    private final MessagesConfig messages;

    public ServiceConnector(MessagesConfig messages) {
        this.messages = messages;
    }

    public void connect(Player player, Service service) {
        final Optional<CloudPlayer> cloudPlayer = CloudAPI.instance().playerManager().find(player.getUniqueId());
        if (cloudPlayer.isEmpty()) {
            return;
        }

        if (cloudPlayer.get().service().filter(service::equals).isPresent()) {
            player.sendMessage(messages.get("already-connected", "%service%", service.name()));
            return;
        }

        if (!service.running() || service.full()) {
            player.sendMessage(messages.get("service-not-available", "%service%", service.name()));
            return;
        }

        player.sendMessage(messages.get("connecting", "%service%", service.name()));
        CloudAPI.instance().playerManager().connectTo(cloudPlayer.get(), service);
    }

    public void connectToBest(Player player, Group group) {
        Placeholders.runningServices(group.name()).stream()
                .filter(service -> !service.full())
                .max(Comparator.comparingInt(Service::playerCount))
                .ifPresentOrElse(service -> connect(player, service),
                        () -> player.sendMessage(messages.get("no-service", "%group%", group.name())));
    }
}
