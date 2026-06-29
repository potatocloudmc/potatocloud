package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record StopServicePacket(String serviceName) implements Packet {

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