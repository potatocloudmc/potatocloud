package net.potatocloud.plugins.platform.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.potatocloud.connector.utils.ServiceConnector;

@Mod("potatocloud")
public final class NeoForgePlugin {

    private static NeoForgePlugin instance;

    private final ServiceConnector connector;

    public NeoForgePlugin() {
        instance = this;
        this.connector = new ServiceConnector();

        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, _ -> connector.initCurrentService());
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, _ -> connector.shutdown());
    }

    public static NeoForgePlugin instance() {
        return instance;
    }

    public ServiceConnector connector() {
        return connector;
    }
}
