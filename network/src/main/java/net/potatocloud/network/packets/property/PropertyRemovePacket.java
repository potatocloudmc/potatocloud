package net.potatocloud.network.packets.property;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record PropertyRemovePacket(String propertyName) implements Packet {

    public static final Codec<PropertyRemovePacket> CODEC = new Codec<>() {

        @Override
        public void encode(PropertyRemovePacket packet, PacketBuffer buf) {
            buf.writeString(packet.propertyName());
        }

        @Override
        public PropertyRemovePacket decode(PacketBuffer buf) {
            return new PropertyRemovePacket(buf.readString());
        }
    };
}