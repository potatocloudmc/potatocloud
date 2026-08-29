package net.potatocloud.connector.properties;

import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packets.property.PropertyAddPacket;
import net.potatocloud.network.packets.property.PropertyUpdatePacket;
import net.potatocloud.network.packets.property.PropertyRemovePacket;
import net.potatocloud.network.packets.property.RequestPropertiesPacket;

import java.util.HashMap;
import java.util.Map;

public class ConnectorPropertiesHolder implements PropertyHolder {

    private final NetworkClient client;

    private final Map<PropertyKey<?>, Object> propertyMap = new HashMap<>();

    public ConnectorPropertiesHolder(NetworkClient client) {
        this.client = client;

        client.send(new RequestPropertiesPacket());

        client.on(PropertyAddPacket.class, ctx -> {
            final PropertyAddPacket packet = ctx.packet();
            propertyMap.put(packet.toKey(), packet.value());
        });

        client.on(PropertyUpdatePacket.class, ctx -> propertyMap.entrySet().stream()
                .filter(e -> e.getKey().name().equals(ctx.packet().propertyName()))
                .findFirst()
                .ifPresent(e -> propertyMap.put(e.getKey(), ctx.packet().propertyValue())));

        client.on(PropertyRemovePacket.class, ctx -> propertyMap.keySet()
                .removeIf(key -> key.name().equals(ctx.packet().propertyName())));
    }

    @Override
    public <T> void set(PropertyKey<T> key, T value, boolean fireEvent) {
        final boolean existing = hasProperty(key);
        PropertyHolder.super.set(key, value, fireEvent);

        if (!existing) {
            client.send(new PropertyAddPacket(key.name(), key.defaultValue(), value));
        } else {
            client.send(new PropertyUpdatePacket(key.name(), value));
        }
    }

    @Override
    public Map<PropertyKey<?>, Object> properties() {
        return propertyMap;
    }

    @Override
    public void removeProperty(PropertyKey<?> key) {
        if (!hasProperty(key)) {
            return;
        }

        PropertyHolder.super.removeProperty(key);
        client.send(new PropertyRemovePacket(key.name()));
    }

    @Override
    public String name() {
        return "Global";
    }
}
