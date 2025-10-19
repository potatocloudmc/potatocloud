package net.potatocloud.connector.platform;

import lombok.Getter;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.platform.PlatformAddPacket;
import net.potatocloud.core.networking.packets.platform.PlatformRemovePacket;
import net.potatocloud.core.networking.packets.platform.PlatformUpdatePacket;
import net.potatocloud.core.networking.packets.platform.RequestPlatformsPacket;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlatformManagerImpl implements PlatformManager {

    private final NetworkClient client;
    private final List<Platform> platforms = new ArrayList<>();

    public PlatformManagerImpl(NetworkClient client) {
        this.client = client;

        // since this class is very short just keep the package listeners here as long as there are not too many and they are not too big
        client.registerPacketListener(PacketIds.PLATFORM_ADD, (NetworkConnection connection, PlatformAddPacket packet) -> {
            platforms.add(packet.getPlatform());
        });

        client.registerPacketListener(PacketIds.PLATFORM_REMOVE, (NetworkConnection connection, PlatformRemovePacket packet) -> {
            platforms.remove(getPlatform(packet.getPlatformName()));
        });

        client.registerPacketListener(PacketIds.PLATFORM_UPDATE, (NetworkConnection connection, PlatformUpdatePacket packet) -> {
            final Platform platform = getPlatform(packet.getPlatform().getName());
            if (platform == null) {
                return;
            }
            platform.setVersions(packet.getPlatform().getVersions());
        });

        client.send(new RequestPlatformsPacket());
    }

    @Override
    public void updatePlatform(Platform platform) {
        client.send(new PlatformUpdatePacket(platform));
    }
}