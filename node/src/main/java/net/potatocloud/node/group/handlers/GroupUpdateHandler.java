package net.potatocloud.node.group.handlers;

import net.potatocloud.api.group.GroupManager;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.group.GroupUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.group.config.GroupStorage;

public final class GroupUpdateHandler implements PacketHandler<GroupUpdatePacket> {

    private final GroupManager groupManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public GroupUpdateHandler(GroupManager groupManager, NetworkServer server, ClusterManagerImpl clusterManager) {
        this.groupManager = groupManager;
        this.server = server;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<GroupUpdatePacket> ctx) {
        final GroupUpdatePacket packet = ctx.packet();

        groupManager.find(packet.groupName()).ifPresent(group -> {
            group.minServices(packet.minOnlineCount());
            group.maxServices(packet.maxOnlineCount());
            group.maxPlayers(packet.maxPlayers());
            group.maxMemory(packet.maxMemory());
            group.fallback(packet.fallback());
            group.startPriority(packet.startPriority());
            group.startPercentage(packet.startPercentage());

            group.templates().clear();
            packet.templates().forEach(group::addTemplate);

            group.customJvmFlags().clear();
            packet.customJvmFlags().forEach(group::addCustomJvmFlag);

            group.propertyMap().clear();
            packet.propertyMap().values().forEach(property -> PropertyUtil.setPropertyUnchecked(group, property));

            if (ctx.connection().type() == ConnectionType.CONNECTOR && groupManager instanceof GroupManagerImpl groupManagerImpl) {
                GroupStorage.save(group, groupManagerImpl.getGroupsPath());
            }
        });

        if (ctx.connection().type() == ConnectionType.CONNECTOR) {
            clusterManager.broadcast(packet);
        }
        server.broadcast().connectors().exclude(ctx.connection()).send(packet);
    }
}
