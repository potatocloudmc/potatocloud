package net.potatocloud.connector.platform;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packet.packets.platform.PlatformAddPacket;
import net.potatocloud.network.packet.packets.platform.PlatformRemovePacket;
import net.potatocloud.network.packet.packets.platform.PlatformUpdatePacket;
import net.potatocloud.network.packet.packets.platform.PlatformsResponsePacket;
import net.potatocloud.network.packet.packets.platform.RequestPlatformsPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlatformManagerImpl implements PlatformManager {

    private final NetworkClient client;
    private final List<Platform> platforms = new ArrayList<>();

    public PlatformManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(PlatformAddPacket.class, ctx -> platforms.add(ctx.packet().platform()));

        client.on(PlatformRemovePacket.class, ctx -> find(ctx.packet().platformName()).ifPresent(platforms::remove));

        client.on(PlatformUpdatePacket.class, ctx ->
                find(ctx.packet().platform().name()).ifPresent(platform -> platform.versions(ctx.packet().platform().versions())));

        client.request(new RequestPlatformsPacket(), PlatformsResponsePacket.class).thenAccept(response -> platforms.addAll(response.platforms()));
    }

    @Override
    public Optional<Platform> find(String name) {
        return platforms.stream().filter(platform -> platform.name().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public List<Platform> platforms() {
        return Collections.unmodifiableList(platforms);
    }

    @Override
    public void create(Platform platform) {
        if (platform == null || exists(platform.name())) {
            return;
        }

        platforms.add(platform);
        client.send(new PlatformAddPacket(platform));
    }

    @Override
    public void delete(Platform platform) {
        if (platform == null) {
            return;
        }

        platforms.remove(platform);
        client.send(new PlatformRemovePacket(platform.name()));
    }

    @Override
    public void update(Platform platform) {
        client.send(new PlatformUpdatePacket(platform));
    }
}
