package net.potatocloud.node.service.start.rules;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceState;

public final class GroupMaxOnlineRule implements ServiceStartRule {

    @Override
    public boolean allows(Group group) {
        final long activeServices = group.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.PREPARING || service.state() == ServiceState.STARTING)
                .count();

        return activeServices < group.maxServices();
    }
}
