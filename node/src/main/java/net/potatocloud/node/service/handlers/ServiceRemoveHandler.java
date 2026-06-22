package net.potatocloud.node.service.handlers;

import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.PacketContext;
import net.potatocloud.network.packet.PacketHandler;
import net.potatocloud.network.packet.packets.service.ServiceRemovePacket;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.ServiceManagerImpl;

public final class ServiceRemoveHandler implements PacketHandler<ServiceRemovePacket> {

    private final ServiceManagerImpl serviceManager;
    private final NetworkServer server;
    private final ScreenManager screenManager;

    public ServiceRemoveHandler(ServiceManagerImpl serviceManager, NetworkServer server, ScreenManager screenManager) {
        this.serviceManager = serviceManager;
        this.server = server;
        this.screenManager = screenManager;
    }

    @Override
    public void handle(PacketContext<ServiceRemovePacket> ctx) {
        if (ctx.connection().type() == ConnectionType.NODE) {
            screenManager.unregister(ctx.packet().serviceName());
        }

        serviceManager.find(ctx.packet().serviceName()).ifPresent(serviceManager::removeService);
        server.broadcast().connectors().exclude(ctx.connection()).send(ctx.packet());
    }
}
