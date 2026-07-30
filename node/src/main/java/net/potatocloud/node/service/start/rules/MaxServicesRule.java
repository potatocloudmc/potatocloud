package net.potatocloud.node.service.start.rules;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.node.config.NodeConfig;

@RequiredArgsConstructor
public class MaxServicesRule implements ServiceStartRule {

    private final NodeConfig config;
    private final ServiceManager serviceManager;

    @Override
    public boolean allows(Group group) {
        final int maxServices = config.service().maxServices();

        // If max services is set to -1 (unlimited), always allow starting new services
        if (maxServices == -1) {
            return true;
        }

        final long activeServices = serviceManager.services().stream()
                .filter(service -> service.running() || service.state() == ServiceState.STARTING)
                .count();

        return activeServices < maxServices;
    }
}
