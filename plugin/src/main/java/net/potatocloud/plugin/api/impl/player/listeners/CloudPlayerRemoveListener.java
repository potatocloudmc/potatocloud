package net.potatocloud.plugin.api.impl.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.player.CloudPlayerRemovePacket;
import net.potatocloud.plugin.api.impl.player.CloudPlayerManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerRemoveListener implements PacketListener<CloudPlayerRemovePacket> {

    private final CloudPlayerManagerImpl playerManager;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerRemovePacket packet) {
        final CloudPlayer player = playerManager.getCloudPlayer(packet.getPlayerUniqueId());
        if (player == null) {
            return;
        }
        playerManager.unregisterPlayerLocal(player);
    }
}
