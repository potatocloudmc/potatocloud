package net.potatocloud.plugins.platform.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.potatocloud.connector.utils.ServiceConnector;

public final class MaintenanceListener {

    private final ServiceConnector connector;

    public MaintenanceListener(ServiceConnector connector) {
        this.connector = connector;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        connector.currentService()
                .filter(service -> service.group().maintenance())
                .filter(_ -> !event.getPlayer().hasPermission(ServiceConnector.MAINTENANCE_BYPASS_PERMISSION))
                .ifPresent(_ -> event.setResult(ResultedEvent.ComponentResult.denied(MiniMessage.miniMessage().deserialize("<red>" + ServiceConnector.MAINTENANCE_MESSAGE))));
    }
}
