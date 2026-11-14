package net.potatocloud.plugins.optional.npc.util;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;

import java.util.List;

public final class Services {

    private Services() {
    }

    public static List<Service> running(String groupName) {
        return CloudAPI.instance().groupManager().find(groupName)
                .map(Group::services)
                .orElse(List.of())
                .stream()
                .filter(Service::running)
                .toList();
    }
}
