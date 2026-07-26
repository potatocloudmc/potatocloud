package net.potatocloud.plugins.platform.fabric.v26_1;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class Fabric26_1Plugin implements ModInitializer, PlatformPlugin {

    private static Fabric26_1Plugin instance;

    private ConnectorAPI api;
    private MinecraftServer server;

    @Override
    public void onInitialize() {
        instance = this;
        this.api = new ConnectorAPI();
    }

    public static Fabric26_1Plugin instance() {
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
    }

    @Override
    public void runTaskLater(Runnable task, int delaySeconds) {
        CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS).execute(() -> server.execute(task));
    }
}
