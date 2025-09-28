package net.potatocloud.node.service;

import net.potatocloud.api.event.events.property.PropertyChangedEvent;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.node.Node;

import java.util.Comparator;
import java.util.List;

public class ServiceStartQueue extends Thread {

    private final ServiceGroupManager groupManager;
    private final ServiceManager serviceManager;

    private boolean isRunning = true;

    public ServiceStartQueue(ServiceGroupManager groupManager, ServiceManager serviceManager) {
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;

        setName("ServiceStartQueue");
        setDaemon(true);

        Node.getInstance().getEventManager().on(PropertyChangedEvent.class, event -> {
            if (!event.getProperty().getName().equals(Property.GAME_STATE.getName())) {
                return;
            }

            if (event.getNewValue() == null || !event.getNewValue().equals("INGAME")) {
                return;
            }

            final Service service = Node.getInstance().getServiceManager().getService(event.getHolderName());
            if (service == null) {
                return;
            }

            final ServiceGroup group = service.getServiceGroup();
            if (group.getOnlineServiceCount() >= group.getMaxOnlineCount()) {
                return;
            }

            serviceManager.startService(service.getServiceGroup());
        });
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                final List<ServiceGroup> groups = groupManager.getAllServiceGroups().stream()
                        .sorted(Comparator.comparingInt(ServiceGroup::getStartPriority).reversed())
                        .toList();

                for (ServiceGroup group : groups) {
                    if (!groupManager.existsServiceGroup(group.getName())) {
                        continue;
                    }

                    final List<Service> services = group.getAllServices().stream()
                            .filter(service -> service.getStatus() == ServiceStatus.RUNNING || service.getStatus() == ServiceStatus.STARTING || service.getStatus() == ServiceStatus.STOPPING)
                            .toList();

                    if (services.size() < group.getMinOnlineCount()) {
                        serviceManager.startService(group);
                        continue;
                    }

                    if (services.size() >= group.getMaxOnlineCount()) {
                        continue;
                    }

                    final int maxPlayers = services.stream().mapToInt(Service::getMaxPlayers).sum();

                    if (maxPlayers == 0) {
                        continue;
                    }

                    final int groupStartPercentage = group.getStartPercentage();
                    if (groupStartPercentage == -1) { // if this is on -1 no server will be started
                        continue;
                    }

                    final int usagePercent = (int) ((group.getOnlinePlayerCount() / (double) maxPlayers) * 100);
                    final boolean hasStarting = services.stream().anyMatch(service -> service.getStatus() == ServiceStatus.STARTING);

                    if (usagePercent >= groupStartPercentage && !hasStarting) {
                        serviceManager.startService(group);
                        break;
                    }
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                isRunning = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void close() {
        isRunning = false;
        interrupt();
    }
}
