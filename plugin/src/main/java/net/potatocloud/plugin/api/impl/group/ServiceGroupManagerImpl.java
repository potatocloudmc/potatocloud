package net.potatocloud.plugin.api.impl.group;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.group.impl.ServiceGroupImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.group.GroupAddPacket;
import net.potatocloud.core.networking.packets.group.GroupDeletePacket;
import net.potatocloud.core.networking.packets.group.GroupUpdatePacket;
import net.potatocloud.core.networking.packets.group.RequestGroupsPacket;
import net.potatocloud.plugin.api.impl.group.listeners.GroupAddListener;
import net.potatocloud.plugin.api.impl.group.listeners.GroupDeleteListener;
import net.potatocloud.plugin.api.impl.group.listeners.GroupUpdateListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ServiceGroupManagerImpl implements ServiceGroupManager {

    private final List<ServiceGroup> groups = new ArrayList<>();
    private final NetworkClient client;

    public ServiceGroupManagerImpl(NetworkClient client) {
        this.client = client;

        client.send(new RequestGroupsPacket());

        client.registerPacketListener(PacketIds.GROUP_ADD, new GroupAddListener(this));
        client.registerPacketListener(PacketIds.GROUP_DELETE, new GroupDeleteListener(this));
        client.registerPacketListener(PacketIds.GROUP_UPDATE, new GroupUpdateListener(this));
    }

    public void addServiceGroup(ServiceGroup group) {
        if (group == null || existsServiceGroup(group.getName())) {
            return;
        }
        groups.add(group);
    }

    @Override
    public ServiceGroup getServiceGroup(String name) {
        return groups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ServiceGroup> getAllServiceGroups() {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public void createServiceGroup(String name, String platformName, String platformVersionName, int minOnlineCount, int maxOnlineCount, int maxPlayers, int maxMemory, boolean fallback, boolean isStatic, int startPriority, int startPercentage, String javaCommand, List<String> customJvmFlags, Set<Property> properties) {
        client.send(new GroupAddPacket(
                name,
                platformName,
                platformVersionName,
                minOnlineCount,
                maxOnlineCount,
                maxPlayers,
                maxMemory,
                fallback,
                isStatic,
                startPriority,
                startPercentage,
                javaCommand,
                customJvmFlags,
                properties
        ));

        final ServiceGroupImpl group = new ServiceGroupImpl(
                name,
                platformName,
                platformVersionName,
                minOnlineCount,
                maxOnlineCount,
                maxPlayers,
                maxMemory,
                fallback,
                isStatic,
                startPriority,
                startPercentage,
                javaCommand,
                customJvmFlags,
                properties
        );

        addServiceGroup(group);
    }


    @Override
    public void deleteServiceGroup(String name) {
        client.send(new GroupDeletePacket(name));

        deleteServiceGroupLocal(name);
    }

    public void deleteServiceGroupLocal(String name) {
        final ServiceGroup group = getServiceGroup(name);
        if (group == null) {
            return;
        }
        groups.remove(group);
    }

    @Override
    public void updateServiceGroup(ServiceGroup group) {
        client.send(new GroupUpdatePacket(
                group.getName(),
                group.getMinOnlineCount(),
                group.getMaxOnlineCount(),
                group.getMaxPlayers(),
                group.getMaxMemory(),
                group.isFallback(),
                group.getStartPriority(),
                group.getStartPercentage(),
                group.getServiceTemplates(),
                group.getProperties(),
                group.getCustomJvmFlags()
        ));
    }

    @Override
    public boolean existsServiceGroup(String groupName) {
        if (groupName == null) {
            return false;
        }
        return groups.stream().anyMatch(group -> group != null && group.getName().equalsIgnoreCase(groupName));
    }
}
