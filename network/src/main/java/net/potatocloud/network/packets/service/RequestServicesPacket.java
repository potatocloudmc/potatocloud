package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record RequestServicesPacket() implements RequestPacket {

    public static final Codec<RequestServicesPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestServicesPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestServicesPacket decode(PacketBuffer buf) {
            return new RequestServicesPacket();
        }
    };
}