package net.potatocloud.node.service.start.rules;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.config.NodeConfig;

public class MaxStartingRule implements ServiceStartRule {

    private final NodeConfig config;
    private final ServiceManager serviceManager;

    public MaxStartingRule(NodeConfig config, ServiceManager serviceManager) {
        this.config = config;
        this.serviceManager = serviceManager;
    }

    @Override
    public boolean allows(Group group) {
        final int maxStarting = config.service().maxStartingServices();

        // if max starting services is set to -1 (unlimited), always allow starting new services
        if (maxStarting == -1) {
            return true;
        }

        final long startingServices = serviceManager.services().stream()
                .filter(service -> service.state() == ServiceState.PREPARING || service.state() == ServiceState.STARTING)
                .count();

        return startingServices < maxStarting;
    }
}
