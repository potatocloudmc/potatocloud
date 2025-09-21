package net.potatocloud.plugin.api.impl.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.player.CloudPlayerAddPacket;
import net.potatocloud.plugin.api.impl.player.CloudPlayerManagerImpl;

@RequiredArgsConstructor
public class CloudPlayerAddListener implements PacketListener<CloudPlayerAddPacket> {

    private final CloudPlayerManagerImpl playerManager;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerAddPacket packet) {
        playerManager.registerPlayerLocal(new CloudPlayerImpl(packet.getUsername(), packet.getUniqueId(), packet.getConnectedProxyName()));
    }
}
