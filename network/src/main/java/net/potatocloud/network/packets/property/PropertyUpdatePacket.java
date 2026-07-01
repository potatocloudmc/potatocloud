package net.potatocloud.network.packets.property;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record PropertyUpdatePacket(String propertyName, Object propertyValue) implements Packet {

    public static final Codec<PropertyUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(PropertyUpdatePacket packet, PacketBuffer buf) {
            buf.writeString(packet.propertyName());
            buf.write(packet.propertyValue(), Object.class);
        }

        @Override
        public PropertyUpdatePacket decode(PacketBuffer buf) {
            return new PropertyUpdatePacket(buf.readString(), buf.read(Object.class));
        }
    };
}