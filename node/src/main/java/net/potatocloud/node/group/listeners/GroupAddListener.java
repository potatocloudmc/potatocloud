package net.potatocloud.node.group.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.impl.ServiceGroupImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.group.GroupAddPacket;
import net.potatocloud.node.group.ServiceGroupManagerImpl;

@RequiredArgsConstructor
public class GroupAddListener implements PacketListener<GroupAddPacket> {

    private final ServiceGroupManagerImpl groupManager;
    private final NetworkServer server;

    @Override
    public void onPacket(NetworkConnection connection, GroupAddPacket packet) {
        groupManager.addServiceGroup(new ServiceGroupImpl(
                packet.getName(),
                packet.getPlatformName(),
                packet.getPlatformVersionName(),
                packet.getJavaCommand(),
                packet.getCustomJvmFlags(),
                packet.getMaxPlayers(),
                packet.getMaxMemory(),
                packet.getMinOnlineCount(),
                packet.getMaxOnlineCount(),
                packet.isStatic(),
                packet.isPrimary(),
                packet.getStartPriority(),
                packet.getStartPercentage(),
                packet.getPropertyMap()
        ));

        server.generateBroadcast().exclude(connection).broadcast(packet);
    }
}
