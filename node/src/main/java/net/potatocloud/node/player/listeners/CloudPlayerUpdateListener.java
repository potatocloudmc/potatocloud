package net.potatocloud.node.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerUpdatePacket;
import net.potatocloud.common.PropertyUtil;

@RequiredArgsConstructor
public class CloudPlayerUpdateListener implements PacketListener<CloudPlayerUpdatePacket> {

    private final CloudPlayerManager playerManager;
    private final NetworkServer server;

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
            PropertyUtil.setPropertyUnchecked(player, property);
        }

        server.generateBroadcast().exclude(connection).broadcast(packet);
    }
}
