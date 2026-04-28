package net.potatocloud.connector.player;

import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.Property;
import net.potatocloud.connector.event.ConnectPlayerWithServiceEvent;
import net.potatocloud.connector.player.listeners.CloudPlayerAddListener;
import net.potatocloud.connector.player.listeners.CloudPlayerRemoveListener;
import net.potatocloud.connector.player.listeners.CloudPlayerUpdateListener;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerAddPacket;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.core.networking.packet.packets.player.RequestCloudPlayersPacket;

import java.util.*;

@Getter
public class CloudPlayerManagerImpl implements CloudPlayerManager {

    private final Set<CloudPlayer> onlinePlayers = new HashSet<>();
    private final NetworkClient client;

    public CloudPlayerManagerImpl(NetworkClient client) {
        this.client = client;

        client.send(new RequestCloudPlayersPacket());

        client.on(CloudPlayerAddPacket.class, new CloudPlayerAddListener(this));
        client.on(CloudPlayerRemovePacket.class, new CloudPlayerRemoveListener(this));
        client.on(CloudPlayerUpdatePacket.class, new CloudPlayerUpdateListener(this));
    }

    public void registerPlayer(CloudPlayer player) {
        if (onlinePlayers.contains(player)) {
            return;
        }
        registerPlayerLocal(player);

        if (!player.getNickname().isEmpty()) player.getPropertyMap().put("nickname", Property.ofString("nickname", player.getNickname()));
        player.getPropertyMap().put("locale", Property.ofLocale("locale", player.getLocale()));
        player.getPropertyMap().put("sessionId", Property.ofString("sessionId", player.getSessionId().toString()));
        player.getPropertyMap().put("sessionStartTime", Property.ofString("sessionStartTime", player.getSessionStartTime().toString()));

        // The service of the player is null here because the player has just connected to the proxy and has not joined a service yet
        // The service will be set later by the proxy plugin once the player successfully connects to a service
        client.send(new CloudPlayerAddPacket(
                player.getUsername(),
                player.getUniqueId(),
                player.getConnectedProxyName(),
                null,
                player.getPropertyMap()
        ));
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

        client.send(new CloudPlayerRemovePacket(player.getUniqueId()));
    }

    public void unregisterPlayerLocal(CloudPlayer player) {
        if (!onlinePlayers.contains(player)) {
            return;
        }
        onlinePlayers.remove(player);
    }

    @Override
    public CloudPlayer getCloudPlayer(String username) {
        return onlinePlayers.stream()
                .filter(player -> player.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CloudPlayer getCloudPlayer(UUID uniqueId) {
        return onlinePlayers.stream()
                .filter(player -> player.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<CloudPlayer> getOnlinePlayers() {
        return Collections.unmodifiableSet(onlinePlayers);
    }

    @Override
    public void connectPlayerWithService(String playerName, String serviceName) {
        CloudAPI.getInstance().getEventManager().call(new ConnectPlayerWithServiceEvent(playerName, serviceName));
    }

    @Override
    public void updatePlayer(CloudPlayer player) {
        client.send(new CloudPlayerUpdatePacket(player.getUniqueId(), player.getConnectedProxyName(),
                player.getConnectedServiceName(), player.getPropertyMap()));
    }
}

