package net.potatocloud.node.group.handlers;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.group.GroupDeletedEvent;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.group.GroupDeletePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.group.GroupManagerImpl;

public final class GroupDeleteHandler implements PacketHandler<GroupDeletePacket> {

    private final GroupManagerImpl groupManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;
    private final EventBus eventBus;

    public GroupDeleteHandler(GroupManagerImpl groupManager, NetworkServer server, ClusterManagerImpl clusterManager, EventBus eventBus) {
        this.groupManager = groupManager;
        this.server = server;
        this.clusterManager = clusterManager;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(PacketContext<GroupDeletePacket> ctx) {
        final GroupDeletePacket packet = ctx.packet();

        if (ctx.connection().type() == ConnectionType.NODE) {
            groupManager.unregisterGroup(packet.groupName());
            server.broadcast().connectors().send(packet);
        } else {
            final boolean deleted = groupManager.deleteLocal(packet.groupName());
            server.broadcast().connectors().exclude(ctx.connection()).send(packet);
            clusterManager.broadcast(packet);

            if (deleted) {
                eventBus.publish(new GroupDeletedEvent(packet.groupName()));
            }
        }
    }
}
