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

    private final ConnectorAPI api;
    private MinecraftServer server;

    public NeoForge1_21_11Plugin() {
        this.api = new ConnectorAPI();

        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> {
            server = event.getServer();
            initCurrentService();
        });

        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, _ -> api.shutdown());
    }

    @Override
    public void onServiceReady(Service service) {
    }

    @Override
    public void runTaskLater(Runnable task, int delaySeconds) {
        CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS).execute(() -> server.execute(task));
    }
}
