package net.potatocloud.network.packets.property;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record PropertyAddPacket(String name, Object defaultValue, Object value) implements Packet {

    public static final Codec<PropertyAddPacket> CODEC = new Codec<>() {

        @Override
        public void encode(PropertyAddPacket packet, PacketBuffer buf) {
            buf.writeString(packet.name());
            buf.writeObject(packet.defaultValue());
            buf.writeObject(packet.value());
        }

        @Override
        public PropertyAddPacket decode(PacketBuffer buf) {
            return new PropertyAddPacket(buf.readString(), buf.readObject(), buf.readObject());
        }
    };

    public PropertyKey<?> toKey() {
        return PropertyKey.of(name, defaultValue);
    }
}