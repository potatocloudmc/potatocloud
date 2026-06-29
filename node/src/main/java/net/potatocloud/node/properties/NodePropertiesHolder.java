package net.potatocloud.node.properties;

import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packets.property.PropertyAddPacket;
import net.potatocloud.network.packets.property.PropertyUpdatePacket;
import net.potatocloud.network.packets.property.RequestPropertiesPacket;
import net.potatocloud.node.cluster.ClusterManagerImpl;

import java.util.HashMap;
import java.util.Map;

public class NodePropertiesHolder implements PropertyHolder {

    private final NetworkServer server;
    private final ClusterManagerImpl clusterManager;

    private final Map<PropertyKey<?>, Object> propertyMap = new HashMap<>();

    public NodePropertiesHolder(NetworkServer server, ClusterManagerImpl clusterManager) {
        this.server = server;
        this.clusterManager = clusterManager;

        server.on(RequestPropertiesPacket.class, ctx -> {
            propertyMap.forEach((key, value) -> ctx.connection().send(new PropertyAddPacket(key.name(), key.defaultValue(), value)));
        });

        server.on(PropertyAddPacket.class, ctx -> {
            final PropertyAddPacket packet = ctx.packet();
            propertyMap.put(packet.toKey(), packet.value());

            server.broadcast().connectors().exclude(ctx.connection()).send(packet);

            if (ctx.connection().type() == ConnectionType.CONNECTOR) {
                clusterManager.broadcast(packet);
            }
        });

        server.on(PropertyUpdatePacket.class, ctx -> {
            propertyMap.entrySet().stream()
                    .filter(e -> e.getKey().name().equals(ctx.packet().propertyName()))
                    .findFirst()
                    .ifPresent(e -> propertyMap.put(e.getKey(), ctx.packet().propertyValue()));

            server.broadcast().connectors().exclude(ctx.connection()).send(ctx.packet());

            if (ctx.connection().type() == ConnectionType.CONNECTOR) {
                clusterManager.broadcast(ctx.packet());
            }
        });
    }

    @Override
    public <T> void set(PropertyKey<T> key, T value, boolean fireEvent) {
        final boolean existing = properties().containsKey(key);
        PropertyHolder.super.set(key, value, fireEvent);

        if (!existing) {
            final PropertyAddPacket packet = new PropertyAddPacket(key.name(), key.defaultValue(), value);

            server.broadcast().connectors().send(packet);
            clusterManager.broadcast(packet);
        } else {
            final PropertyUpdatePacket packet = new PropertyUpdatePacket(key.name(), value);

            server.broadcast().connectors().send(packet);
            clusterManager.broadcast(packet);
        }
    }

    @Override
    public Map<PropertyKey<?>, Object> properties() {
        return propertyMap;
    }

    @Override
    public String name() {
        return "Global";
    }
}
