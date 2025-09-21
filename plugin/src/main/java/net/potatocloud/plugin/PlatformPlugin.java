package net.potatocloud.plugin;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.core.networking.packets.service.ServiceStartedPacket;
import net.potatocloud.plugin.api.impl.PluginCloudAPI;

public interface PlatformPlugin {

    void runTaskLater(Runnable task, int delaySeconds);

    void onServiceReady(Service service);

    default void initCurrentService() {
        final Service currentService = CloudAPI.getInstance().getServiceManager().getCurrentService();
        // service manager is still null or the services have not finished loading
        if (currentService == null) {
            // retry after 1 second
            runTaskLater(this::initCurrentService, 1);
            return;
        }

        PluginCloudAPI.getInstance().getClient().send(new ServiceStartedPacket(currentService.getName()));
        onServiceReady(currentService);
    }
}
