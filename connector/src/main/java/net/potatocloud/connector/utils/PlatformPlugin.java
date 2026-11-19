package net.potatocloud.connector.utils;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.network.packets.service.ServiceStartedPacket;

public interface PlatformPlugin {

    void onServiceReady(Service service);

    default void initCurrentService() {
        final Service service = CloudAPI.instance().serviceManager().current()
                .orElseThrow(() -> new IllegalStateException("Current service not found"));

        ConnectorAPI.getInstance()
                .client()
                .send(new ServiceStartedPacket(service.name()));

        onServiceReady(service);
    }
}
