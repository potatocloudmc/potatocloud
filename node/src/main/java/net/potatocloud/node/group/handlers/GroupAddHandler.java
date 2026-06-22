package net.potatocloud.node.group.handlers;

import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.group.GroupAddPacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.group.GroupManagerImpl;

public final class GroupAddHandler implements PacketHandler<GroupAddPacket> {

    private final GroupManagerImpl groupManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    public GroupAddHandler(GroupManagerImpl groupManager, NetworkServer server, ClusterManagerImpl clusterManager) {
        this.groupManager = groupManager;
        this.server = server;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<GroupAddPacket> ctx) {
        final GroupAddPacket packet = ctx.packet();

        if (groupManager.exists(packet.group().name())) {
            return;
        }

        if (ctx.connection().type() == ConnectionType.NODE) {
            groupManager.registerGroup(packet.group());
            server.broadcast().connectors().send(packet);
        } else {
            groupManager.addGroup(packet.group());
            server.broadcast().connectors().exclude(ctx.connection()).send(packet);
            clusterManager.broadcast(packet);
        }
    }
}
