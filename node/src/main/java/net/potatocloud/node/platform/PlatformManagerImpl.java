package net.potatocloud.node.platform;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.packets.platform.PlatformAddPacket;
import net.potatocloud.network.packet.packets.platform.PlatformRemovePacket;
import net.potatocloud.network.packet.packets.platform.PlatformUpdatePacket;
import net.potatocloud.network.packet.packets.platform.RequestPlatformsPacket;
import net.potatocloud.network.packet.packets.platform.PlatformsResponsePacket;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlatformManagerImpl implements PlatformManager {

    private final Logger logger;
    private final NetworkServer server;
    private final PlatformFileHandler fileHandler;
    private final List<Platform> platforms;

    public PlatformManagerImpl(Logger logger, NetworkServer server) {
        this.logger = logger;
        this.server = server;
        this.fileHandler = new PlatformFileHandler(logger);
        this.platforms = fileHandler.loadPlatformsFile();

        server.on(RequestPlatformsPacket.class, ctx -> ctx.reply(new PlatformsResponsePacket(platforms())));
        server.on(PlatformUpdatePacket.class, ctx -> find(ctx.packet().platform().name()).ifPresent(platform -> platform.versions(ctx.packet().platform().versions())));
        server.on(PlatformAddPacket.class, ctx -> addPlatform(ctx.packet().platform()));
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
        fileHandler.savePlatform(platform);
        server.broadcast().connectors().send(new PlatformAddPacket(platform));

        logger.info("Platform &a" + platform.name() + " &7was successfully created");
    }

    @Override
    public void delete(Platform platform) {
        if (platform == null) {
            return;
        }

        platforms.remove(platform);
        fileHandler.deletePlatform(platform);
        server.broadcast().connectors().send(new PlatformRemovePacket(platform.name()));

        logger.info("Platform &a" + platform.name() + " &7was successfully deleted");
    }

    @Override
    public void update(Platform platform) {
        server.broadcast().connectors().send(new PlatformUpdatePacket(platform));
        fileHandler.savePlatform(platform);
    }

    public void addPlatform(Platform platform) {
        if (platform == null || exists(platform.name())) {
            return;
        }

        platforms.add(platform);
        fileHandler.savePlatform(platform);
    }
}
