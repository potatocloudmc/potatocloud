package net.potatocloud.plugins.platform.neoforge.v1_21_11;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mod("potatocloud")
public final class NeoForge1_21_11Plugin implements PlatformPlugin {

    private static NeoForge1_21_11Plugin instance;

    private final ConnectorAPI api;
    private MinecraftServer server;
    private Service currentService;

    public NeoForge1_21_11Plugin() {
        instance = this;
        this.api = new ConnectorAPI();

        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> {
            server = event.getServer();
            initCurrentService();
        });

        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, _ -> api.shutdown());
    }

    @Override
    public void onServiceReady(Service service) {
        this.currentService = service;
    }

    public static NeoForge1_21_11Plugin instance() {
        return instance;
    }

    public Integer maxPlayers() {
        return currentService == null ? null : currentService.maxPlayers();
    }

    @Override
    public void runTaskLater(Runnable task, int delaySeconds) {
        CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS).execute(() -> server.execute(task));
    }
}
