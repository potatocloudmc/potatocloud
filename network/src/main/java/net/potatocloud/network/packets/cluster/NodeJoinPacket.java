package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.time.Instant;

public record NodeJoinPacket(String nodeName, String host, int port, Instant startedAt, String nodeVersion, String clusterToken) implements Packet {

    public static final Codec<NodeJoinPacket> CODEC = new Codec<>() {

        @Override
        public void encode(NodeJoinPacket packet, PacketBuffer buf) {
            buf.writeString(packet.nodeName());
            buf.writeString(packet.host());
            buf.writeVarInt(packet.port());
            buf.write(packet.startedAt(), Instant.class);
            buf.writeString(packet.nodeVersion());
            buf.writeString(packet.clusterToken());
        }

        @Override
        public NodeJoinPacket decode(PacketBuffer buf) {
            return new NodeJoinPacket(buf.readString(), buf.readString(), buf.readVarInt(), buf.read(Instant.class), buf.readString(), buf.readString());
        }
    };
}
