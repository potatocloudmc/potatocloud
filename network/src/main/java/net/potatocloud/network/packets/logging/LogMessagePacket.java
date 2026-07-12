package net.potatocloud.network.packets.logging;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record LogMessagePacket(String level, String message) implements Packet {

    public static final Codec<LogMessagePacket> CODEC = new Codec<>() {

        @Override
        public void encode(LogMessagePacket packet, PacketBuffer buf) {
            buf.writeString(packet.level());
            buf.writeString(packet.message());
        }

        @Override
        public LogMessagePacket decode(PacketBuffer buf) {
            return new LogMessagePacket(buf.readString(), buf.readString());
        }
    };
}

