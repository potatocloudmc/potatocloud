package net.potatocloud.node.service.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketListener;
import net.potatocloud.network.packet.packets.service.StartServicePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.service.ServiceManagerImpl;

import java.util.Optional;

@RequiredArgsConstructor
public class StartServiceListener implements PacketListener<StartServicePacket> {

    private final ServiceManagerImpl serviceManager;
    private final GroupManager groupManager;
    private final ClusterManagerImpl clusterManager;

    @Override
    public void handle(PacketContext<StartServicePacket> ctx) {
        final Optional<Group> group = groupManager.find(ctx.packet().groupName());
        if (group.isEmpty()) {
            return;
        }

        final Optional<ClusterNode> node = group.get().node();
        if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
            clusterManager.sendTo(node.get().name(), ctx.packet());
            return;
        }

        if (!serviceManager.hasEnoughMemory(group.get())) {
            serviceManager.logMemoryWarning(group.get());
            return;
        }

        serviceManager.startService(group.get().name(), ctx.packet().requestId());
    }
}
