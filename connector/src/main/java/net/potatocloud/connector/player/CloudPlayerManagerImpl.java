package net.potatocloud.connector.player;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.PublishTarget;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.event.ConnectPlayerWithServiceEvent;
import net.potatocloud.connector.player.handlers.CloudPlayerUpdateHandler;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packets.player.CloudPlayerAddPacket;
import net.potatocloud.network.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.network.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.network.packets.player.CloudPlayersResponsePacket;
import net.potatocloud.network.packets.player.RequestCloudPlayersPacket;

import java.util.*;

public class CloudPlayerManagerImpl implements CloudPlayerManager {

    private final Set<CloudPlayer> onlinePlayers = new HashSet<>();
    private final NetworkClient client;

    public CloudPlayerManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(CloudPlayerAddPacket.class, ctx -> registerPlayerLocal(ctx.packet().player()));
        client.on(CloudPlayerRemovePacket.class, ctx -> find(ctx.packet().playerUniqueId()).ifPresent(this::unregisterPlayerLocal));
        client.on(CloudPlayerUpdatePacket.class, new CloudPlayerUpdateHandler(this));

        client.request(new RequestCloudPlayersPacket(), CloudPlayersResponsePacket.class).thenAccept(response -> response.players().forEach(this::registerPlayerLocal));
    }

    public void registerPlayer(CloudPlayer player) {
        if (onlinePlayers.contains(player)) {
            return;
        }

        registerPlayerLocal(player);

        // the service of the player is null here because the player has just connected to the proxy and has not joined a service yet
        // it will be set later by the proxy plugin once the player successfully connects to a service
        client.send(new CloudPlayerAddPacket(player));
    }

    public void registerPlayerLocal(CloudPlayer player) {
        if (onlinePlayers.contains(player)) {
            return;
        }
        onlinePlayers.add(player);
    }

    public void unregisterPlayer(CloudPlayer player) {
        if (!onlinePlayers.contains(player)) {
            return;
        }
        unregisterPlayerLocal(player);

        client.send(new CloudPlayerRemovePacket(player.uniqueId()));
    }

    public void unregisterPlayerLocal(CloudPlayer player) {
        if (!onlinePlayers.contains(player)) {
            return;
        }
        onlinePlayers.remove(player);
    }

    @Override
    public Optional<CloudPlayer> find(UUID uniqueId) {
        return onlinePlayers.stream().filter(player -> player.uniqueId().equals(uniqueId)).findFirst();
    }

    @Override
    public Optional<CloudPlayer> find(String username) {
        return onlinePlayers.stream().filter(player -> player.username().equals(username)).findFirst();
    }

    @Override
    public Set<CloudPlayer> players() {
        return Collections.unmodifiableSet(onlinePlayers);
    }

    @Override
    public void connectTo(CloudPlayer player, Service service) {
        CloudAPI.instance().eventBus().publish(new ConnectPlayerWithServiceEvent(player.username(), service.name()), PublishTarget.LOCAL);
    }

    @Override
    public void update(CloudPlayer player) {
        client.send(new CloudPlayerUpdatePacket(
                player.uniqueId(),
                player.proxy().name(),
                player.service().map(Service::name).orElse(null),
                player.properties()
        ));
    }
}