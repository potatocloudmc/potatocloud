package net.potatocloud.node.group.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.group.GroupAddPacket;
import net.potatocloud.core.networking.packets.group.RequestGroupsPacket;

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
                    group.getMinOnlineCount(),
                    group.getMaxOnlineCount(),
                    group.getMaxPlayers(),
                    group.getMaxMemory(),
                    group.isFallback(),
                    group.isStatic(),
                    group.getStartPriority(),
                    group.getStartPercentage(),
                    group.getJavaCommand(),
                    group.getCustomJvmFlags(),
                    group.getPropertyMap()
            ));
        }
    }
}
