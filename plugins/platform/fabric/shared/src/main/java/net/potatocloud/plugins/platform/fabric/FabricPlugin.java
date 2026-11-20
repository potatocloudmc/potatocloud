package net.potatocloud.plugins.platform.fabric;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.potatocloud.connector.utils.ServiceConnector;

public final class FabricPlugin implements ModInitializer {

    private static FabricPlugin instance;

    private ServiceConnector connector;

    @Override
    public void onInitialize() {
        instance = this;
        this.connector = new ServiceConnector();
    }

    public static FabricPlugin instance() {
        return instance;
    }

    public void serverStarted(MinecraftServer server) {
        connector.initCurrentService();
    }

    public void serverStopped() {
        connector.shutdown();
    }

    public ServiceConnector connector() {
        return connector;
    }
}
