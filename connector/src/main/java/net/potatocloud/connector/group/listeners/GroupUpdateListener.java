package net.potatocloud.connector.group.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.group.GroupUpdatePacket;

@RequiredArgsConstructor
public class GroupUpdateListener implements PacketListener<GroupUpdatePacket> {

    private final ServiceGroupManager groupManager;

    @Override
    public void onPacket(NetworkConnection connection, GroupUpdatePacket packet) {
        final ServiceGroup group = groupManager.getServiceGroup(packet.getGroupName());
        if (group == null) {
            return;
        }

        group.setMinOnlineCount(packet.getMinOnlineCount());
        group.setMaxOnlineCount(packet.getMaxOnlineCount());
        group.setMaxPlayers(packet.getMaxPlayers());
        group.setMaxMemory(packet.getMaxMemory());
        group.setFallback(packet.isFallback());
        group.setStartPriority(packet.getStartPriority());
        group.setStartPercentage(packet.getStartPercentage());

        group.getServiceTemplates().clear();
        packet.getServiceTemplates().forEach(group::addServiceTemplate);

        group.getCustomJvmFlags().clear();
        packet.getCustomJvmFlags().forEach(group::addCustomJvmFlag);

        group.getPropertyMap().clear();
        for (Property<?> property : packet.getPropertyMap().values()) {
            group.setProperty((Property) property, property.getValue(), false);
        }
    }
}
