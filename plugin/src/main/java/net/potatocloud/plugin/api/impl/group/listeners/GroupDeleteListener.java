package net.potatocloud.plugin.api.impl.group.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.PacketListener;
import net.potatocloud.core.networking.packets.group.GroupDeletePacket;
import net.potatocloud.plugin.api.impl.group.ServiceGroupManagerImpl;

@RequiredArgsConstructor
public class GroupDeleteListener implements PacketListener<GroupDeletePacket> {

    private final ServiceGroupManagerImpl groupManager;

    @Override
    public void onPacket(NetworkConnection connection, GroupDeletePacket packet) {
        groupManager.deleteServiceGroupLocal(packet.getGroupName());
    }
}
