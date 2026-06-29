package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record NodeJoinRejectPacket(String reason) implements Packet {

    public static final Codec<NodeJoinRejectPacket> CODEC = new Codec<>() {

        @Override
        public void encode(NodeJoinRejectPacket packet, PacketBuffer buf) {
            buf.writeString(packet.reason());
        }

        @Override
        public NodeJoinRejectPacket decode(PacketBuffer buf) {
            return new NodeJoinRejectPacket(buf.readString());
        }
    };
}
