package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record StartServicePacket(String groupName) implements RequestPacket {

    public static final Codec<StartServicePacket> CODEC = new Codec<>() {

        @Override
        public void encode(StartServicePacket packet, PacketBuffer buf) {
            buf.writeString(packet.groupName());
        }

        @Override
        public StartServicePacket decode(PacketBuffer buf) {
            return new StartServicePacket(buf.readString());
        }
    };
}