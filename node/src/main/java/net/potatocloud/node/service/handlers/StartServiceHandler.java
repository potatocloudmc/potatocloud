package net.potatocloud.node.service.handlers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.packets.service.StartServiceResponsePacket;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.StartServicePacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.service.ServiceManagerImpl;

import java.util.Optional;

public final class StartServiceHandler implements PacketHandler<StartServicePacket> {

    private final ServiceManagerImpl serviceManager;
    private final GroupManager groupManager;
    private final ClusterManagerImpl clusterManager;

    public StartServiceHandler(ServiceManagerImpl serviceManager, GroupManager groupManager, ClusterManagerImpl clusterManager) {
        this.serviceManager = serviceManager;
        this.groupManager = groupManager;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<StartServicePacket> ctx) {
        final Optional<Group> group = groupManager.find(ctx.packet().groupName());
        if (group.isEmpty()) {
            return;
        }

        final Optional<ClusterNode> node = group.get().node();
        if (node.isPresent() && !clusterManager.isLocal(node.get().name())) {
            clusterManager.request(node.get().name(), ctx.packet(), StartServiceResponsePacket.class)
                    .thenAccept(ctx::reply);
            return;
        }

        if (!serviceManager.hasEnoughMemory(group.get())) {
            serviceManager.logMemoryWarning(group.get());
            return;
        }

        final Service service = serviceManager.startService(group.get().name());
        ctx.reply(new StartServiceResponsePacket(service));
    }
}
