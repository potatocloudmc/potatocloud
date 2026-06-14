package net.potatocloud.node.service.start.rule.rules;

import net.potatocloud.api.group.Group;
import net.potatocloud.node.service.ServiceManagerImpl;
import net.potatocloud.node.service.start.rule.ServiceStartRule;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MaxMemoryRule implements ServiceStartRule {

    private final ServiceManagerImpl serviceManager;

    private final Set<String> memoryWarnedGroups = ConcurrentHashMap.newKeySet();

    public MaxMemoryRule(ServiceManagerImpl serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public boolean allows(Group group) {
        final boolean enough = serviceManager.hasEnoughMemory(group);

        if (!enough && memoryWarnedGroups.add(group.name())) {
            serviceManager.logMemoryWarning(group);
        }

        if (enough) {
            memoryWarnedGroups.remove(group.name());
        }

        return enough;
    }
}
