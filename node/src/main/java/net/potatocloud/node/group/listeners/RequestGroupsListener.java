package net.potatocloud.node.group.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.group.GroupAddPacket;
import net.potatocloud.core.networking.packet.packets.group.RequestGroupsPacket;

@RequiredArgsConstructor
public class RequestGroupsListener implements PacketListener<RequestGroupsPacket> {

    private final ServiceGroupManager groupManager;

    @Override
    public void onPacket(NetworkConnection connection, RequestGroupsPacket packet) {
        for (ServiceGroup group : groupManager.getAllServiceGroups()) {
            connection.send(new GroupAddPacket(
                    group.getName(),
                    group.getPlatformName(),
                    group.getPlatformVersionName(),
                    group.getJavaCommand(),
                    group.getCustomJvmFlags(),
                    group.getMaxPlayers(),
                    group.getMaxMemory(),
                    group.getMinOnlineCount(),
                    group.getMaxOnlineCount(),
                    group.isStatic(),
                    group.isPrimary(),
                    group.getStartPriority(),
                    group.getStartPercentage(),
                    group.getServiceTemplates(),
                    group.getPropertyMap()
            ));
        }
    }
}
