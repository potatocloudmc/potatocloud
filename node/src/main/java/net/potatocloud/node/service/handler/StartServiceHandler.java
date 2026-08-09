package net.potatocloud.node.service.handler;

import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.network.packets.service.StartServiceResponsePacket;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.StartServicePacket;
import net.potatocloud.node.service.NodeServiceManager;

import java.util.Optional;

public final class StartServiceHandler implements PacketHandler<StartServicePacket> {

    private final NodeServiceManager serviceManager;
    private final GroupManager groupManager;

    public StartServiceHandler(NodeServiceManager serviceManager, GroupManager groupManager) {
        this.serviceManager = serviceManager;
        this.groupManager = groupManager;
    }

    @Override
    public void handle(PacketContext<StartServicePacket> ctx) {
        final Optional<Group> group = groupManager.find(ctx.packet().groupName());
        if (group.isEmpty()) {
            return;
        }

        serviceManager.start(group.get()).thenAccept(service -> {
            if (service != null) {
                ctx.reply(new StartServiceResponsePacket(service));
            }
        });
    }
}
