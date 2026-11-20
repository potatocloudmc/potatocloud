package net.potatocloud.plugins.platform.limbo;

import net.potatocloud.connector.utils.ServiceConnector;
import net.potatocloud.plugins.platform.limbo.listener.MaintenanceListener;
import net.potatocloud.plugins.platform.limbo.listener.MaxPlayersListener;

public final class LimboPlugin extends com.loohp.limbo.plugins.LimboPlugin {

    private ServiceConnector connector;

    @Override
    public void onLoad() {
        connector = new ServiceConnector();
    }

    @Override
    public void onEnable() {
        connector.initCurrentService();
        getServer().getEventsManager().registerEvents(this, new MaxPlayersListener(connector));
        getServer().getEventsManager().registerEvents(this, new MaintenanceListener(connector));
    }

    @Override
    public void onDisable() {
        connector.shutdown();
    }
}
