package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record NodeJoinPacket(String nodeName, String host, int port, long startedAt, String nodeVersion, String clusterToken) implements Packet {

    public static final Codec<NodeJoinPacket> CODEC = new Codec<>() {

        @Override
        public void encode(NodeJoinPacket packet, PacketBuffer buf) {
            buf.writeString(packet.nodeName());
            buf.writeString(packet.host());
            buf.writeInt(packet.port());
            buf.writeLong(packet.startedAt());
            buf.writeString(packet.nodeVersion());
            buf.writeString(packet.clusterToken());
        }

        @Override
        public NodeJoinPacket decode(PacketBuffer buf) {
            return new NodeJoinPacket(buf.readString(), buf.readString(), buf.readInt(), buf.readLong(), buf.readString(), buf.readString());
        }
    };
}
