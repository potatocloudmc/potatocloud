package net.potatocloud.eventbus;

import net.potatocloud.api.event.Event;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.EventHandler;
import net.potatocloud.api.event.PublishTarget;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.event.EventPacket;

public class ServerEventBus implements EventBus {

    private final DefaultEventBus local = new DefaultEventBus();
    private final NetworkServer server;

    public ServerEventBus(NetworkServer server) {
        this.server = server;
        server.on(EventPacket.class, ctx -> {
            if (ctx.connection().type() == ConnectionType.CONNECTOR) {
                local.publish(JsonEventCodec.decode(ctx.packet()));
                server.broadcast().connectors().exclude(ctx.connection()).send(ctx.packet());
            }
        });
    }

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        local.subscribe(eventType, handler);
    }

    @Override
    public <T extends Event> boolean unsubscribe(Class<T> eventType, EventHandler<T> handler) {
        return local.unsubscribe(eventType, handler);
    }

    @Override
    public <T extends Event> void publish(T event) {
        publish(event, PublishTarget.BOTH);
    }

    @Override
    public <T extends Event> void publish(T event, PublishTarget target) {
        switch (target) {
            case LOCAL -> local.publish(event);
            case NETWORK -> server.broadcast().connectors().send(JsonEventCodec.encode(event));
            case BOTH -> {
                local.publish(event);
                server.broadcast().connectors().send(JsonEventCodec.encode(event));
            }
        }
    }
}
