package net.potatocloud.connector.utils;

import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.network.packets.service.ServiceStartedPacket;

import java.util.Optional;

public final class ServiceConnector {

    public static final String MAX_PLAYERS_BYPASS_PERMISSION = "potatocloud.maxplayers.bypass";
    public static final String SERVER_FULL_MESSAGE = "The server has reached its maximum players!";

    private final ConnectorAPI api;
    private Service currentService;

    public ServiceConnector() {
        this.api = new ConnectorAPI();
    }

    public void initCurrentService() {
        currentService = api.serviceManager().current()
                .orElseThrow(() -> new IllegalStateException("Current service not found"));

        api.client().send(new ServiceStartedPacket(currentService.name()));
    }

    public Optional<Service> currentService() {
        return Optional.ofNullable(currentService);
    }

    public ConnectorAPI api() {
        return api;
    }

    public void shutdown() {
        api.shutdown();
    }
}
