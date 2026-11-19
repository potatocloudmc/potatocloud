package net.potatocloud.plugins.platform.fabric;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;

public final class FabricPlugin implements ModInitializer, PlatformPlugin {

    private static FabricPlugin instance;

    private ConnectorAPI api;
    private Service currentService;

    @Override
    public void onInitialize() {
        instance = this;
        this.api = new ConnectorAPI();
    }

    public static FabricPlugin instance() {
        return instance;
    }

    public void serverStarted(MinecraftServer server) {
        initCurrentService();
    }

    public void serverStopped() {
        api.shutdown();
    }

    @Override
    public void onServiceReady(Service service) {
        this.currentService = service;
    }

    public Integer maxPlayers() {
        return currentService == null ? null : currentService.maxPlayers();
    }
}
