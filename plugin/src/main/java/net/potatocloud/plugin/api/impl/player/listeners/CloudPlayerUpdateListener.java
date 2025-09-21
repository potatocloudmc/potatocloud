package net.potatocloud.plugin.api.impl.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.plugin.api.impl.player.CloudPlayerManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerUpdateListener implements PacketListener<CloudPlayerUpdatePacket> {

    private final CloudPlayerManagerImpl playerManager;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerUpdatePacket packet) {
        final CloudPlayerImpl player = (CloudPlayerImpl) playerManager.getCloudPlayer(packet.getPlayerUniqueId());
        if (player == null) {
            return;
        }

        player.setConnectedProxyName(packet.getConnectedProxyName());
        player.setConnectedServiceName(packet.getConnectedServiceName());

        player.getProperties().clear();
        for (Property property : packet.getProperties()) {
            player.setProperty(property, property.getValue(), false);
        }
    }
}

