package net.potatocloud.plugins.platform.fabric.v1_21_11;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class Fabric1_21_11Plugin implements ModInitializer, PlatformPlugin {

    private static Fabric1_21_11Plugin instance;

    private ConnectorAPI api;
    private MinecraftServer server;
    private Service currentService;

    @Override
    public void onInitialize() {
        instance = this;
        this.api = new ConnectorAPI();
    }

    public static Fabric1_21_11Plugin instance() {
        return instance;
    }

    public void serverStarted(MinecraftServer server) {
        this.server = server;
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

    @Override
    public void runTaskLater(Runnable task, int delaySeconds) {
        CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS).execute(() -> server.execute(task));
    }
}
