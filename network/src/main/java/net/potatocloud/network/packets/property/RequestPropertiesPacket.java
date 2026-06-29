package net.potatocloud.network.packets.property;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record RequestPropertiesPacket() implements Packet {

    public static final Codec<RequestPropertiesPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestPropertiesPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestPropertiesPacket decode(PacketBuffer buf) {
            return new RequestPropertiesPacket();
        }
    };
}