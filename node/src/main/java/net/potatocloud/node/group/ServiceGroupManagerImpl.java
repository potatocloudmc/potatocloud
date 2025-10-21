package net.potatocloud.node.group;

import lombok.Getter;
import lombok.SneakyThrows;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.group.impl.ServiceGroupImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.group.GroupAddPacket;
import net.potatocloud.core.networking.packets.group.GroupUpdatePacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.group.listeners.GroupAddListener;
import net.potatocloud.node.group.listeners.GroupDeleteListener;
import net.potatocloud.node.group.listeners.GroupUpdateListener;
import net.potatocloud.node.group.listeners.RequestGroupsListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ServiceGroupManagerImpl implements ServiceGroupManager {

    private final List<ServiceGroup> groups = new ArrayList<>();

    @Getter
    private final Path groupsPath;

    private final NetworkServer server;

    public ServiceGroupManagerImpl(Path groupsPath, NetworkServer server) {
        this.groupsPath = groupsPath;
        this.server = server;

        server.registerPacketListener(PacketIds.REQUEST_GROUPS, new RequestGroupsListener(this));
        server.registerPacketListener(PacketIds.GROUP_UPDATE, new GroupUpdateListener(this));
        server.registerPacketListener(PacketIds.GROUP_ADD, new GroupAddListener(this));
        server.registerPacketListener(PacketIds.GROUP_DELETE, new GroupDeleteListener(this));
    }

    @Override
    public ServiceGroup getServiceGroup(String name) {
        return groups.stream()
                .filter(cloudServiceGroup -> cloudServiceGroup.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ServiceGroup> getAllServiceGroups() {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public void createServiceGroup(
            String name,
            String platformName,
            String platformVersionName,
            int minOnlineCount,
            int maxOnlineCount,
            int maxPlayers,
            int maxMemory,
            boolean fallback,
            boolean isStatic,
            int startPriority,
            int startPercentage,
            String javaCommand,
            List<String> customJvmFlags,
            Map<String, Property<?>> propertyMap
    ) {

        if (existsServiceGroup(name)) {
            return;
        }

        final ServiceGroup serviceGroup = new ServiceGroupImpl(
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
                propertyMap
        );

        addServiceGroup(serviceGroup);

        // send group add packet to clients
        server.broadcastPacket(new GroupAddPacket(
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
                propertyMap
        ));

        Node.getInstance().getLogger().info("Group &a" + name + " &7was successfully created");
    }

    public void addServiceGroup(ServiceGroup group) {
        if (group == null || existsServiceGroup(group.getName())) {
            return;
        }

        for (String templateName : group.getServiceTemplates()) {
            Node.getInstance().getTemplateManager().createTemplate(templateName);
        }

        ServiceGroupStorage.saveToFile(group, groupsPath);
        groups.add(group);
    }

    @Override
    public void deleteServiceGroup(String name) {
        final ServiceGroup group = getServiceGroup(name);

        if (group == null || !groups.contains(group)) {
            return;
        }

        group.getAllServices().forEach(Service::shutdown);

        groups.remove(group);

        final Path filePath = groupsPath.resolve(name + ".yml");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateServiceGroup(ServiceGroup group) {
        ServiceGroupStorage.saveToFile(group, groupsPath);

        server.broadcastPacket(new GroupUpdatePacket(
                group.getName(),
                group.getMinOnlineCount(),
                group.getMaxOnlineCount(),
                group.getMaxPlayers(),
                group.getMaxMemory(),
                group.isFallback(),
                group.getStartPriority(),
                group.getStartPercentage(),
                group.getServiceTemplates(),
                group.getPropertyMap(),
                group.getCustomJvmFlags()
        ));
    }

    @Override
    public boolean existsServiceGroup(String groupName) {
        if (groupName == null) {
            return false;
        }
        return groups.stream().anyMatch(serviceGroup -> serviceGroup != null && serviceGroup.getName().equalsIgnoreCase(groupName));
    }


    @SneakyThrows
    public void loadGroups() {
        if (!Files.exists((groupsPath))) {
            return;
        }

        try (Stream<Path> paths = Files.list(groupsPath)) {
            paths.filter(path -> path.toString().endsWith(".yml")).forEach(path -> groups.add(ServiceGroupStorage.loadFromFile(path)));
        }
    }
}
