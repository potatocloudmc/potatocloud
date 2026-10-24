package net.potatocloud.connector.group.handlers;

import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.group.impl.GroupImpl;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.group.GroupUpdatePacket;

public final class GroupUpdateHandler implements PacketHandler<GroupUpdatePacket> {

    private final GroupManager groupManager;

    public GroupUpdateHandler(GroupManager groupManager) {
        this.groupManager = groupManager;
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

            group.customJvmFlags().clear();
            packet.customJvmFlags().forEach(group::addCustomJvmFlag);

            group.properties().clear();
            packet.propertyMap().forEach((key, value) -> PropertyUtil.setUnchecked(group, key, value));
        });
    }
}
