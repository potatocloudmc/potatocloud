package net.potatocloud.node.service.start.condition;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;

import java.util.List;

public final class PlayerUsageCondition implements ServiceStartCondition {

    @Override
    public boolean shouldStart(Group group) {
        final List<Service> activeServices = group.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.STARTING)
                .toList();

        if (group.startPercentage() == -1) {
            return false;
        }

        final int maxPlayers = activeServices.stream()
                .mapToInt(Service::maxPlayers)
                .sum();

        if (maxPlayers <= 0) {
            return false;
        }

        final int usagePercent = (int) ((group.players().size() / (double) maxPlayers) * 100);

        return usagePercent >= group.startPercentage();
    }
}
