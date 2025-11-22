package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.service.ServiceStartedPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.service.ServiceImpl;
import net.potatocloud.node.service.ServiceMemoryUpdateTask;
import net.potatocloud.node.service.ServiceProcessChecker;

@RequiredArgsConstructor
public class ServiceStartedListener implements PacketListener<ServiceStartedPacket> {

    private final ServiceManager serviceManager;
    private final Logger logger;
    private final EventManager eventManager;

    @Override
    public void onPacket(NetworkConnection connection, ServiceStartedPacket packet) {
        final Service service = serviceManager.getService(packet.getServiceName());
        if (service == null) {
            return;
        }

        logger.info("Service &a" + packet.getServiceName() + "&7 is now &aonline");

        service.setStatus(ServiceStatus.RUNNING);
        service.update();

        // call service started event
        eventManager.call(new ServiceStartedEvent(packet.getServiceName()));

        //start process checker
        if (service instanceof ServiceImpl impl) {
            impl.setProcessChecker(new ServiceProcessChecker(impl));
            impl.getProcessChecker().start();
        }

        new ServiceMemoryUpdateTask(service, Node.getInstance().getServer()).start();
    }
}
