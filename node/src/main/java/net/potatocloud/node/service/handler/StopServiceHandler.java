package net.potatocloud.node.service.handler;

import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.network.protocol.PacketContext;
import net.potatocloud.network.protocol.PacketHandler;
import net.potatocloud.network.packets.service.StopServicePacket;
import net.potatocloud.network.packets.service.StopServiceResponsePacket;

public final class StopServiceHandler implements PacketHandler<StopServicePacket> {

    private final ServiceManager serviceManager;

    public StopServiceHandler(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void handle(PacketContext<StopServicePacket> ctx) {
        serviceManager.find(ctx.packet().serviceName()).ifPresent(service -> serviceManager.stop(service).thenAccept(response -> ctx.reply(new StopServiceResponsePacket())));
    }
}
