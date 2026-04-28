package net.potatocloud.connector.group.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.impl.ServiceGroupImpl;
import net.potatocloud.connector.group.ServiceGroupManagerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.group.GroupAddPacket;

@RequiredArgsConstructor
public class GroupAddListener implements PacketListener<GroupAddPacket> {

    private final ServiceGroupManagerImpl groupManager;

    @Override
    public void onPacket(NetworkConnection connection, GroupAddPacket packet) {
        if (groupManager.existsServiceGroup(packet.getName())) {
            return;
        }

        final ServiceGroupImpl group = new ServiceGroupImpl(
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
                packet.getServiceTemplates(),
                packet.getPropertyMap()
        );

        groupManager.addServiceGroup(group);
    }
}
