package net.potatocloud.connector.player.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.connector.player.CloudPlayerManagerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.player.CloudPlayerAddPacket;

import java.util.Locale;

@RequiredArgsConstructor
public class CloudPlayerAddListener implements PacketListener<CloudPlayerAddPacket> {

    private final CloudPlayerManagerImpl playerManager;

    @Override
    public void onPacket(NetworkConnection connection, CloudPlayerAddPacket packet) {
        playerManager.registerPlayerLocal(new CloudPlayerImpl(packet.getUsername(),"", packet.getUniqueId(), /*packet.getLocale()*/Locale.ENGLISH, packet.getConnectedProxyName(), packet.getConnectedServiceName(), packet.getPropertyMap()));
    }
}
