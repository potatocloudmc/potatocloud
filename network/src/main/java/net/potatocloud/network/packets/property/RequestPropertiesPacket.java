package net.potatocloud.network.packets.property;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record RequestPropertiesPacket() implements RequestPacket {

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