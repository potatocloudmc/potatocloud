package net.potatocloud.node.service.start.rule;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.config.NodeConfig;

public final class MaxStartingRule implements ServiceStartRule {

    private final NodeConfig config;
    private final ServiceManager serviceManager;

    public MaxStartingRule(NodeConfig config, ServiceManager serviceManager) {
        this.config = config;
        this.serviceManager = serviceManager;
    }

    @Override
    public boolean allows(Group group) {
        final int maxStarting = config.service().maxStartingServices();

        if (maxStarting == -1) {
            return true;
        }

        final long startingServices = serviceManager.services().stream()
                .filter(service -> service.state() == ServiceState.PREPARING
                        || service.state() == ServiceState.STARTING)
                .count();

        return startingServices < maxStarting;
    }
}
