package net.potatocloud.node.service.start.rule.rules;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.node.service.ServiceManagerImpl;
import net.potatocloud.node.service.start.rule.ServiceStartRule;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class MaxMemoryRule implements ServiceStartRule {

    private final ServiceManagerImpl serviceManager;

    private final Set<String> memoryWarnedGroups = ConcurrentHashMap.newKeySet();

    @Override
    public boolean allows(ServiceGroup group) {
        final boolean enough = serviceManager.hasEnoughMemory(group);

        if (!enough && memoryWarnedGroups.add(group.getName())) {
            serviceManager.logMemoryWarning(group);
        }

        if (enough) {
            memoryWarnedGroups.remove(group.getName());
        }

        return enough;
    }
}
