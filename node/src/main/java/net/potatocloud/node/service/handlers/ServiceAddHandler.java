package net.potatocloud.node.service.handlers;

import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.ServiceAddPacket;
import net.potatocloud.node.Node;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.impl.RemoteServiceScreen;
import net.potatocloud.node.service.ServiceManagerImpl;

public final class ServiceAddHandler implements PacketHandler<ServiceAddPacket> {

    private final ServiceManagerImpl serviceManager;
    private final NetworkServer server;
    private final ScreenManager screenManager;
    private final ClusterManagerImpl clusterManager;

    public ServiceAddHandler(ServiceManagerImpl serviceManager, NetworkServer server, ScreenManager screenManager, ClusterManagerImpl clusterManager) {
        this.serviceManager = serviceManager;
        this.server = server;
        this.screenManager = screenManager;
        this.clusterManager = clusterManager;
    }

    @Override
    public void handle(PacketContext<ServiceAddPacket> ctx) {
        final ServiceAddPacket packet = ctx.packet();

        if (serviceManager.find(packet.service().name()).isPresent()) {
            return;
        }

        serviceManager.addService(packet.service());

        if (ctx.connection().type() == ConnectionType.NODE) {
            final Console console = Node.instance().console();
            final Screen screen = new RemoteServiceScreen(packet.service(), console, clusterManager);
            screenManager.register(screen);
        }

        server.broadcast().connectors().send(new ServiceAddPacket(packet.service()));
    }
}
