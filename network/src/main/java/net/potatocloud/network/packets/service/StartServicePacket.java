package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record StartServicePacket(String groupName, String requestId) implements Packet {

    public static final Codec<StartServicePacket> CODEC = new Codec<>() {

        @Override
        public void encode(StartServicePacket packet, PacketBuffer buf) {
            buf.writeString(packet.groupName());
            buf.writeString(packet.requestId());
        }

        @Override
        public StartServicePacket decode(PacketBuffer buf) {
            return new StartServicePacket(buf.readString(), buf.readString());
        }
    };
}