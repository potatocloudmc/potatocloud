package net.potatocloud.node.service.start.conditions;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;

import java.util.List;

public class PlayerUsageCondition implements ServiceStartCondition {

    @Override
    public boolean shouldStart(Group group) {
        final List<Service> activeServices = group.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.STARTING)
                .toList();

        // if start percentage is set to -1 (disabled), do not start a new service
        if (group.startPercentage() == -1) {
            return false;
        }

        final int maxPlayers = activeServices.stream()
                .mapToInt(Service::maxPlayers)
                .sum();

        // if there are no available player slots, do not start a service
        if (maxPlayers <= 0) {
            return false;
        }

        // get the current usage percentage of the group
        final int usagePercent = (int) ((group.players().size() / (double) maxPlayers) * 100);

        return usagePercent >= group.startPercentage();
    }
}
