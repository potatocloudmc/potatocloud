package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record HeartbeatPacket(String nodeName) implements Packet {

    public static final Codec<HeartbeatPacket> CODEC = new Codec<>() {

        @Override
        public void encode(HeartbeatPacket packet, PacketBuffer buf) {
            buf.writeString(packet.nodeName());
        }

        @Override
        public HeartbeatPacket decode(PacketBuffer buf) {
            return new HeartbeatPacket(buf.readString());
        }
    };
}
