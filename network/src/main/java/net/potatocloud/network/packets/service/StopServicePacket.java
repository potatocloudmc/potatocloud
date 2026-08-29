package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record StopServicePacket(String serviceName) implements RequestPacket {

    public static final Codec<StopServicePacket> CODEC = new Codec<>() {

        @Override
        public void encode(StopServicePacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
        }

        @Override
        public StopServicePacket decode(PacketBuffer buf) {
            return new StopServicePacket(buf.readString());
        }
    };
}