package net.potatocloud.node.platform.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.platform.PlatformAddPacket;
import net.potatocloud.core.networking.packets.platform.RequestPlatformsPacket;

@RequiredArgsConstructor
public class RequestPlatformsListener implements PacketListener<RequestPlatformsPacket> {

    private final PlatformManager platformManager;

    @Override
    public void onPacket(NetworkConnection connection, RequestPlatformsPacket packet) {
        for (Platform platform : platformManager.getPlatforms()) {
            connection.send(new PlatformAddPacket(platform));
        }
    }
}
