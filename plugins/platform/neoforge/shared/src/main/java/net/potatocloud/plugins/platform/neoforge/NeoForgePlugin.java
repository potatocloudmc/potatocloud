package net.potatocloud.plugins.platform.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.connector.utils.PlatformPlugin;

@Mod("potatocloud")
public final class NeoForgePlugin implements PlatformPlugin {

    private static NeoForgePlugin instance;

    private final ConnectorAPI api;
    private Service currentService;

    public NeoForgePlugin() {
        instance = this;
        this.api = new ConnectorAPI();

        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, _ -> initCurrentService());

        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, _ -> api.shutdown());
    }

    @Override
    public void onServiceReady(Service service) {
        this.currentService = service;
    }

    public static NeoForgePlugin instance() {
        return instance;
    }

    public Integer maxPlayers() {
        return currentService == null ? null : currentService.maxPlayers();
    }
}
