package net.potatocloud.node.group.handlers;

import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.group.impl.GroupImpl;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.group.GroupUpdatePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.Node;
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
            if (group instanceof GroupImpl groupImpl) {
                groupImpl.nodeName(packet.nodeName());
                groupImpl.platform(packet.platformName(), packet.platformVersionName());
            }
            group.javaCommand(packet.javaCommand());
            group.minServices(packet.minOnlineCount());
            group.maxServices(packet.maxOnlineCount());
            group.maxPlayers(packet.maxPlayers());
            group.maxMemory(packet.maxMemory());
            group.fallback(packet.fallback());
            group.startPriority(packet.startPriority());
            group.startPercentage(packet.startPercentage());

            group.templates().clear();
            packet.templates().forEach(group::addTemplate);
            group.templates().forEach(Node.instance().templateManager()::create);

            group.customJvmFlags().clear();
            packet.customJvmFlags().forEach(group::addCustomJvmFlag);

            group.properties().clear();
            packet.propertyMap().forEach((key, value) -> PropertyUtil.setUnchecked(group, key, value));

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
