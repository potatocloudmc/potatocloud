package net.potatocloud.node.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.node.Node;

@RequiredArgsConstructor
public class CloudPlayerUpdateListener implements PacketListener<CloudPlayerUpdatePacket> {

    private final CloudPlayerManager playerManager;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerUpdatePacket packet) {
        final CloudPlayerImpl player = (CloudPlayerImpl) playerManager.getCloudPlayer(packet.getPlayerUniqueId());
        if (player == null) {
            return;
        }

        player.setConnectedProxyName(packet.getConnectedProxyName());
        player.setConnectedServiceName(packet.getConnectedServiceName());

        player.getPropertyMap().clear();
        for (Property<?> property : packet.getPropertyMap().values()) {
            player.setProperty((Property) property, property.getValue(), false);
        }

        Node.getInstance().getServer().getConnectedSessions().stream()
                .filter(networkConnection -> !networkConnection.equals(connection))
                .forEach(networkConnection -> networkConnection.send(packet));
    }
}
