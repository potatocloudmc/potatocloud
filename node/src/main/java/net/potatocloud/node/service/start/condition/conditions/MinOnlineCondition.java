package net.potatocloud.node.service.start.condition.conditions;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.service.start.condition.ServiceStartCondition;

public class MinOnlineCondition implements ServiceStartCondition {

    @Override
    public boolean shouldStart(Group group) {
        final long serviceCount = group.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.PREPARING || service.state() == ServiceState.STARTING || service.state() == ServiceState.STOPPING)
                .count();

        return group.minServices() > serviceCount;
    }
}
