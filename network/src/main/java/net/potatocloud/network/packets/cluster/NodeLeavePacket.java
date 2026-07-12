package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record NodeLeavePacket(String nodeName) implements Packet {

    public static final Codec<NodeLeavePacket> CODEC = new Codec<>() {

        @Override
        public void encode(NodeLeavePacket packet, PacketBuffer buf) {
            buf.writeString(packet.nodeName());
        }

        @Override
        public NodeLeavePacket decode(PacketBuffer buf) {
            return new NodeLeavePacket(buf.readString());
        }
    };
}
