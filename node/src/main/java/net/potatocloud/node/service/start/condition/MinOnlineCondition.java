package net.potatocloud.node.service.start.condition;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceState;

public final class MinOnlineCondition implements ServiceStartCondition {

    @Override
    public boolean shouldStart(Group group) {
        final long serviceCount = group.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.PREPARING || service.state() == ServiceState.STARTING || service.state() == ServiceState.STOPPING)
                .count();

        return group.minServices() > serviceCount;
    }
}
