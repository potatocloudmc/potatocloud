package net.potatocloud.node.service;

import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.node.Node;

public class ServiceProcessChecker extends Thread {

    private final ServiceImpl service;

    public ServiceProcessChecker(ServiceImpl service) {
        this.service = service;
        setDaemon(true);
        setName("ServiceProcessChecker-" + service.getName());
    }

    @Override
    public void run() {
        while (!isInterrupted() && service.isOnline() && service.getServerProcess() != null && service.getServerProcess().isAlive()) {
            // if everything is fine, check again after 2 seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        if (!isInterrupted()) {
            service.getLogger().info("Service &a" + service.getName() + " &7seems to be offline...");

            final NetworkServer server = Node.getInstance().getServer();
            final EventManager eventManager = Node.getInstance().getEventManager();

            if (server != null && eventManager != null) {
                eventManager.call(new ServiceStoppingEvent(service.getName()));
            }

            service.cleanup();
        }
    }
}
