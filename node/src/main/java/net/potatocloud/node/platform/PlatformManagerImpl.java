package net.potatocloud.node.platform;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.platform.PlatformUpdatePacket;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.listeners.RequestPlatformsListener;

import java.util.Collections;
import java.util.List;

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

        server.registerPacketListener(PacketIds.REQUEST_PLATFORMS, new RequestPlatformsListener(this));

        logger.info("Loaded &a" + platforms.size() + " &7platforms");
    }

    public List<Platform> getPlatforms() {
        return Collections.unmodifiableList(platforms);
    }

    @Override
    public void updatePlatform(Platform platform) {
        server.broadcastPacket(new PlatformUpdatePacket(platform));

        fileHandler.updatePlatform(platform);
    }
}