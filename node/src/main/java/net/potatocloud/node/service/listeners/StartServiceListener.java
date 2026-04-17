package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.service.StartServicePacket;
import net.potatocloud.node.service.ServiceManagerImpl;

@RequiredArgsConstructor
public class StartServiceListener implements PacketListener<StartServicePacket> {

    private final ServiceManagerImpl serviceManager;
    private final ServiceGroupManager groupManager;

    @Override
    public void onPacket(NetworkConnection connection, StartServicePacket packet) {
        final ServiceGroup group = groupManager.getServiceGroup(packet.getGroupName());
        if (group == null) {
            return;
        }

        if (!serviceManager.hasEnoughMemory(group)) {
            serviceManager.logMemoryWarning(group);
            return;
        }

        serviceManager.startServiceInternal(group.getName(), packet.getRequestId());
    }
}
