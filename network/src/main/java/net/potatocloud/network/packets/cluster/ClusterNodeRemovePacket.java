package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ClusterNodeRemovePacket(String nodeName) implements Packet {

    public static final Codec<ClusterNodeRemovePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ClusterNodeRemovePacket packet, PacketBuffer buf) {
            buf.writeString(packet.nodeName());
        }

        @Override
        public ClusterNodeRemovePacket decode(PacketBuffer buf) {
            return new ClusterNodeRemovePacket(buf.readString());
        }
    };
}
