package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.api.utils.TimeFormatter;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.service.ServiceStartedPacket;
import net.potatocloud.node.Node;
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

        logger.debug("Startup of &a" + packet.getServiceName() + "&7 took " + TimeFormatter.formatAsDuration(System.currentTimeMillis() - service.getStartTimestamp()));

        service.setStatus(ServiceStatus.RUNNING);
        service.update();

        eventManager.call(new ServiceStartedEvent(packet.getServiceName()));

        if (service instanceof ServiceImpl serviceImpl) {
            serviceImpl.setProcessChecker(new ServiceProcessChecker(serviceImpl));
            serviceImpl.getProcessChecker().start();
        }

        new ServiceMemoryUpdateTask(service, Node.getInstance().getServer()).start();
    }
}
