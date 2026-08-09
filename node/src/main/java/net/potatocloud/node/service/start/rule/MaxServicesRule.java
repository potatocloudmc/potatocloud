package net.potatocloud.node.service.start.rule;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.config.NodeConfig;

public final class MaxServicesRule implements ServiceStartRule {

    private final NodeConfig config;
    private final ServiceManager serviceManager;

    public MaxServicesRule(NodeConfig config, ServiceManager serviceManager) {
        this.config = config;
        this.serviceManager = serviceManager;
    }

    @Override
    public boolean allows(Group group) {
        final int maxServices = config.service().maxServices();

        if (maxServices == -1) {
            return true;
        }

        final long activeServices = serviceManager.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.STARTING)
                .count();

        return activeServices < maxServices;
    }
}
