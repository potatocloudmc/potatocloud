package net.potatocloud.network.packets.template;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record RequestTemplatesPacket() implements RequestPacket {

    public static final Codec<RequestTemplatesPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestTemplatesPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestTemplatesPacket decode(PacketBuffer buf) {
            return new RequestTemplatesPacket();
        }
    };
}
