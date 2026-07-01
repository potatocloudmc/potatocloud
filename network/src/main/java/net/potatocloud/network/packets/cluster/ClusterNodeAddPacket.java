package net.potatocloud.network.packets.cluster;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ClusterNodeAddPacket(ClusterNode node) implements Packet {

    public static final Codec<ClusterNodeAddPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ClusterNodeAddPacket packet, PacketBuffer buf) {
            buf.write(packet.node(), ClusterNode.class);
        }

        @Override
        public ClusterNodeAddPacket decode(PacketBuffer buf) {
            return new ClusterNodeAddPacket(buf.read(ClusterNode.class));
        }
    };
}
