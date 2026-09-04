package net.potatocloud.node.service.handler;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceAddPacket;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.NodeServiceManager;

public final class ServiceAddHandler implements PacketHandler<ServiceAddPacket> {

    private final NodeServiceManager serviceManager;
    private final NetworkServer server;
    private final ScreenManager screenManager;

    public ServiceAddHandler(
            NodeServiceManager serviceManager,
            NetworkServer server,
            ScreenManager screenManager
    ) {
        this.serviceManager = serviceManager;
        this.server = server;
        this.screenManager = screenManager;
    }

    @Override
    public void handle(PacketContext<ServiceAddPacket> ctx) {
        final ServiceAddPacket packet = ctx.packet();

        if (serviceManager.find(packet.service().name()).isPresent()) {
            return;
        }

        serviceManager.addService(packet.service());

        if (ctx.connection().type() == ConnectionType.NODE) {
            final String nodeName = packet.service().node().map(ClusterNode::name).orElse(null);
            final Screen screen = Screen.remoteService(packet.service().name(), nodeName);
            screenManager.register(screen);
        }

        server.broadcast().connectors().send(new ServiceAddPacket(packet.service()));
    }
}
