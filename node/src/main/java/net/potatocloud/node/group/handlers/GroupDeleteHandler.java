package net.potatocloud.node.group.handlers;

import lombok.RequiredArgsConstructor;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.group.GroupDeletePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.group.GroupManagerImpl;

@RequiredArgsConstructor
public class GroupDeleteHandler implements PacketHandler<GroupDeletePacket> {

    private final GroupManagerImpl groupManager;
    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    @Override
    public void handle(PacketContext<GroupDeletePacket> ctx) {
        final GroupDeletePacket packet = ctx.packet();

        if (ctx.connection().type() == ConnectionType.NODE) {
            groupManager.unregisterGroup(packet.groupName());
            server.broadcast().connectors().send(packet);
        } else {
            groupManager.deleteLocal(packet.groupName());
            server.broadcast().connectors().exclude(ctx.connection()).send(packet);
            clusterManager.broadcast(packet);
        }
    }
}
