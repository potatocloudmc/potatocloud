package net.potatocloud.connector.player;

import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.player.CloudPlayerAddPacket;
import net.potatocloud.core.networking.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.core.networking.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.core.networking.packets.player.RequestCloudPlayersPacket;
import net.potatocloud.connector.event.ConnectPlayerWithServiceEvent;
import net.potatocloud.connector.player.listeners.CloudPlayerAddListener;
import net.potatocloud.connector.player.listeners.CloudPlayerRemoveListener;
import net.potatocloud.connector.player.listeners.CloudPlayerUpdateListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class CloudPlayerManagerImpl implements CloudPlayerManager {

    private final Set<CloudPlayer> onlinePlayers = new HashSet<>();
    private final NetworkClient client;

    public CloudPlayerManagerImpl(NetworkClient client) {
        this.client = client;

        client.send(new RequestCloudPlayersPacket());

        client.registerPacketListener(PacketIds.PLAYER_ADD, new CloudPlayerAddListener(this));
        client.registerPacketListener(PacketIds.PLAYER_REMOVE, new CloudPlayerRemoveListener(this));
        client.registerPacketListener(PacketIds.PLAYER_UPDATE, new CloudPlayerUpdateListener(this));
    }

    public void registerPlayer(CloudPlayer player) {
        if (onlinePlayers.contains(player)) {
            return;
        }
        registerPlayerLocal(player);

        client.send(new CloudPlayerAddPacket(player.getUsername(), player.getUniqueId(), player.getConnectedProxyName(), null));
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
                player.getConnectedServiceName(), player.getProperties()));
    }
}

