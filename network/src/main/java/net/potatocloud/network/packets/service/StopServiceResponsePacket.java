package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

public record StopServiceResponsePacket() implements ResponsePacket {

    public static final Codec<StopServiceResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(StopServiceResponsePacket packet, PacketBuffer buf) {
        }

        @Override
        public StopServiceResponsePacket decode(PacketBuffer buf) {
            return new StopServiceResponsePacket();
        }
    };
}
