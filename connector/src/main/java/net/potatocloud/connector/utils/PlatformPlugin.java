package net.potatocloud.connector.utils;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.connector.ConnectorAPI;
import net.potatocloud.core.networking.packets.service.ServiceStartedPacket;

public interface PlatformPlugin {

    void runTaskLater(Runnable task, int delaySeconds);

    void onServiceReady(Service service);

    default void initCurrentService() {
        final CloudAPI api = CloudAPI.getInstance();

        if (api.getServiceManager() == null) {
            runTaskLater(this::initCurrentService, 1);
            return;
        }

        final Service currentService = api.getServiceManager().getCurrentService();

        if (currentService == null) {
            runTaskLater(this::initCurrentService, 1);
            return;
        }

        ConnectorAPI.getInstance()
                .getClient()
                .send(new ServiceStartedPacket(currentService.getName()));

        onServiceReady(currentService);
    }
}
