package net.potatocloud.plugin.api.impl.platform;

import lombok.Getter;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.platform.PlatformAddPacket;
import net.potatocloud.core.networking.packets.platform.PlatformRemovePacket;
import net.potatocloud.core.networking.packets.platform.RequestPlatformsPacket;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlatformManagerImpl implements PlatformManager {

    private final List<Platform> platforms = new ArrayList<>();

    public PlatformManagerImpl(NetworkClient client) {
        client.registerPacketListener(PacketIds.PLATFORM_ADD, (NetworkConnection connection, PlatformAddPacket packet) -> {
            platforms.add(packet.getPlatform());
        });

        client.registerPacketListener(PacketIds.PLATFORM_REMOVE, (NetworkConnection connection, PlatformRemovePacket packet) -> {
            platforms.remove(getPlatform(packet.getPlatformName()));
        });

        client.send(new RequestPlatformsPacket());
    }
}