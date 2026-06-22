package net.potatocloud.connector.group.handlers;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.group.GroupUpdatePacket;

@RequiredArgsConstructor
public class GroupUpdateHandler implements PacketHandler<GroupUpdatePacket> {

    private final GroupManager groupManager;

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
        });
    }
}
