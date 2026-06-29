package net.potatocloud.connector.utils;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.network.packets.service.ServiceStartedPacket;

import java.util.Optional;

public interface PlatformPlugin {

    void runTaskLater(Runnable task, int delaySeconds);

    void onServiceReady(Service service);

    default void initCurrentService() {
        final CloudAPI api = CloudAPI.instance();

        if (api.serviceManager() == null) {
            runTaskLater(this::initCurrentService, 1);
            return;
        }

        final Optional<Service> current = api.serviceManager().current();

        if (current.isEmpty() || current.get().group() == null) {
            runTaskLater(this::initCurrentService, 1);
            return;
        }

        final Service service = current.get();

        ConnectorAPI.getInstance()
                .client()
                .send(new ServiceStartedPacket(service.name()));

        onServiceReady(service);
    }
}
