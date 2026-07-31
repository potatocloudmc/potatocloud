package net.potatocloud.node.service.helper;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServiceIds {

    private ServiceIds() {
    }

    public static int nextId(Group group, Map<String, Service> services) {
        final Set<Integer> usedIds = services.values().stream()
                .filter(service -> service.group().equals(group))
                .map(Service::id)
                .collect(Collectors.toSet());

        int id = 1;
        while (usedIds.contains(id)) {
            id++;
        }
        return id;
    }
}
