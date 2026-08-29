package net.potatocloud.node.cluster;

import net.potatocloud.api.event.Event;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.EventHandler;
import net.potatocloud.api.event.PublishTarget;
import net.potatocloud.eventbus.JsonEventCodec;
import net.potatocloud.eventbus.ServerEventBus;
import net.potatocloud.network.packets.event.EventPacket;

public class ClusterEventBus implements EventBus {

    private final ServerEventBus bus;
    private final ClusterManagerImpl clusterManager;

    public ClusterEventBus(ServerEventBus bus, ClusterManagerImpl clusterManager) {
        this.bus = bus;
        this.clusterManager = clusterManager;
    }

    // called when an event packet arrives from another cluster node
    public void publishEventFromCluster(EventPacket packet) {
        bus.publish(JsonEventCodec.decode(packet));
    }

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        bus.subscribe(eventType, handler);
    }

    @Override
    public <T extends Event> boolean unsubscribe(Class<T> eventType, EventHandler<T> handler) {
        return bus.unsubscribe(eventType, handler);
    }

    @Override
    public <T extends Event> void publish(T event) {
        publish(event, PublishTarget.BOTH);
    }

    @Override
    public <T extends Event> void publish(T event, PublishTarget target) {
        bus.publish(event, target);

        if (target != PublishTarget.LOCAL) {
            // send to other cluster nodes
            clusterManager.broadcast(JsonEventCodec.encode(event));
        }
    }
}
