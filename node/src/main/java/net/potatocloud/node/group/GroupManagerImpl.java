package net.potatocloud.node.group;

import lombok.Getter;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.service.Service;
import net.potatocloud.common.FileUtils;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.packets.group.GroupAddPacket;
import net.potatocloud.network.packet.packets.group.GroupDeletePacket;
import net.potatocloud.network.packet.packets.group.GroupUpdatePacket;
import net.potatocloud.network.packet.packets.group.RequestGroupsPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.group.config.GroupStorage;
import net.potatocloud.node.group.listeners.GroupAddListener;
import net.potatocloud.node.group.listeners.GroupDeleteListener;
import net.potatocloud.node.group.listeners.GroupUpdateListener;
import net.potatocloud.node.group.listeners.RequestGroupsListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GroupManagerImpl implements GroupManager {

    private final List<Group> groups = new ArrayList<>();

    @Getter
    private final Path groupsPath;

    private final NetworkServer server;
    private final Logger logger;
    private final ClusterManagerImpl clusterManager;

    public GroupManagerImpl(Path groupsPath, NetworkServer server, Logger logger, ClusterManagerImpl clusterManager) {
        this.groupsPath = groupsPath;
        this.server = server;
        this.logger = logger;
        this.clusterManager = clusterManager;

        server.on(RequestGroupsPacket.class, new RequestGroupsListener(this));
        server.on(GroupUpdatePacket.class, new GroupUpdateListener(this, server, clusterManager));
        server.on(GroupAddPacket.class, new GroupAddListener(this, server, clusterManager));
        server.on(GroupDeletePacket.class, new GroupDeleteListener(this, server, clusterManager));

        loadGroups();
    }

    @Override
    public Optional<Group> find(String name) {
        return groups.stream()
                .filter(group -> group.name().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Group> groups() {
        return Collections.unmodifiableList(groups);
    }

    @Override
    public void create(Group group) {
        if (exists(group.name())) {
            return;
        }

        addGroup(group);

        server.broadcast().connectors().send(new GroupAddPacket(group));
        clusterManager.broadcast(new GroupAddPacket(group));

        logger.info("Group &a" + group.name() + " &7was successfully created");
    }

    public void addGroup(Group group) {
        if (group == null || exists(group.name())) {
            return;
        }

        for (String templateName : group.templates()) {
            Node.instance().templateManager().createTemplate(templateName);
        }

        GroupStorage.save(group, groupsPath);
        groups.add(group);
    }

    public void registerGroup(Group group) {
        if (group == null || exists(group.name())) {
            return;
        }
        groups.add(group);
    }

    public void unregisterGroup(String name) {
        groups.removeIf(group -> group.name().equalsIgnoreCase(name));
    }

    @Override
    public void delete(Group group) {
        if (!deleteLocal(group.name())) {
            return;
        }

        server.broadcast().connectors().send(new GroupDeletePacket(group.name()));
        clusterManager.broadcast(new GroupDeletePacket(group.name()));
    }

    public boolean deleteLocal(String name) {
        final Optional<Group> group = find(name);

        if (group.isEmpty() || !groups.contains(group.get())) {
            return false;
        }

        for (Service service : group.get().services()) {
            CloudAPI.instance().serviceManager().stop(service); // todo
        }

        groups.remove(group.get());

        final Path filePath = groupsPath.resolve(name + ".yml");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Failed to delete group file for: " + name);
        }
        return true;
    }

    @Override
    public void update(Group group) {
        GroupStorage.save(group, groupsPath);

        final GroupUpdatePacket updatePacket = new GroupUpdatePacket(
                group.name(),
                group.customJvmFlags(),
                group.maxPlayers(),
                group.maxMemory(),
                group.minServices(),
                group.maxServices(),
                group.fallback(),
                group.startPriority(),
                group.startPercentage(),
                group.templates(),
                group.propertyMap()
        );
        server.broadcast().connectors().send(updatePacket);
        clusterManager.broadcast(updatePacket);
    }

    private void loadGroups() {
        if (Files.notExists(groupsPath)) {
            return;
        }

        FileUtils.list(groupsPath).stream()
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".yml"))
                .forEach(path -> {
                    try {
                        groups.add(GroupStorage.load(path));
                    } catch (Exception e) {
                        logger.error("Failed to load group file&8: &a" + path.getFileName());
                    }
                });
    }
}
