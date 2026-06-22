package net.potatocloud.connector.group;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.connector.group.listeners.GroupUpdateListener;
import net.potatocloud.network.packet.packets.group.GroupsResponsePacket;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packet.packets.group.GroupAddPacket;
import net.potatocloud.network.packet.packets.group.GroupDeletePacket;
import net.potatocloud.network.packet.packets.group.GroupUpdatePacket;
import net.potatocloud.network.packet.packets.group.RequestGroupsPacket;

import java.util.*;

public class GroupManagerImpl implements GroupManager {

    private final List<Group> groups = new ArrayList<>();
    private final NetworkClient client;

    public GroupManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(GroupAddPacket.class, ctx -> {
            if (!exists(ctx.packet().group().name())) {
                addGroup(ctx.packet().group());
            }
        });

        client.on(GroupDeletePacket.class, ctx -> deleteLocal(ctx.packet().groupName()));
        client.on(GroupUpdatePacket.class, new GroupUpdateListener(this));

        client.request(new RequestGroupsPacket(), GroupsResponsePacket.class).thenAccept(response -> response.groups().forEach(this::addGroup));
    }

    public void addGroup(Group group) {
        if (group == null || exists(group.name())) {
            return;
        }
        groups.add(group);
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

        client.send(new GroupAddPacket(group));
        addGroup(group);
    }

    @Override
    public void delete(Group group) {
        client.send(new GroupDeletePacket(group.name()));
        deleteLocal(group.name());
    }

    public void deleteLocal(String name) {
        find(name).ifPresent(groups::remove);
    }

    @Override
    public void update(Group group) {
        client.send(new GroupUpdatePacket(
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
        ));
    }
}
