package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

public final class PropertyKeySerializer implements TypeSerializer<PropertyKey> {

    @Override
    public void write(PacketBuffer buffer, PropertyKey key) {
        buffer.writeString(key.name());
        buffer.write(key.defaultValue(), Object.class);
    }

    @Override
    public PropertyKey<?> read(PacketBuffer buffer) {
        return new PropertyKey<>(buffer.readString(), buffer.read(Object.class));
    }
}