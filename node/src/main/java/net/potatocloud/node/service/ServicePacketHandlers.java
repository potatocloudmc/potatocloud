package net.potatocloud.node.service;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.service.*;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.group.GroupManagerImpl;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.service.handler.*;

public final class ServicePacketHandlers {

    private ServicePacketHandlers() {
    }

    public static void register(
            NetworkServer server,
            NodeServiceManager serviceManager,
            GroupManagerImpl groupManager,
            EventBus eventBus,
            ClusterManagerImpl clusterManager,
            Logger logger,
            ScreenManager screenManager
    ) {
        server.on(RequestServicesPacket.class, ctx -> ctx.reply(new ServicesResponsePacket(serviceManager.services())));
        server.on(ServiceAddPacket.class, new ServiceAddHandler(serviceManager, server, screenManager, clusterManager));
        server.on(ServiceRemovePacket.class, new ServiceRemoveHandler(serviceManager, server, screenManager));
        server.on(ServiceStartedPacket.class, new ServiceStartedHandler(serviceManager, logger, eventBus, clusterManager, server));
        server.on(ServiceUpdatePacket.class, new ServiceUpdateHandler(serviceManager, server, clusterManager));
        server.on(ServiceStartingPacket.class, new ServiceStartingHandler(logger, serviceManager));
        server.on(StartServicePacket.class, new StartServiceHandler(serviceManager, groupManager));
        server.on(StopServicePacket.class, new StopServiceHandler(serviceManager));
        server.on(ServiceExecuteCommandPacket.class, new ServiceExecuteCommandHandler(serviceManager));
        server.on(ServiceCopyPacket.class, new ServiceCopyHandler(serviceManager));
        server.on(ServiceMemoryUpdatePacket.class, new ServiceMemoryUpdateHandler(serviceManager, server));
    }
}
