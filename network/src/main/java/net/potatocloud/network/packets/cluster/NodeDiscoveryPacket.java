package net.potatocloud.network.packets.cluster;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.List;

public record NodeDiscoveryPacket(List<ClusterNode> nodes) implements Packet {

    public static final Codec<NodeDiscoveryPacket> CODEC = new Codec<>() {

        @Override
        public void encode(NodeDiscoveryPacket packet, PacketBuffer buf) {
            buf.write(packet.nodes(), CollectionSerializers.list(ClusterNode.class));
        }

        @Override
        public NodeDiscoveryPacket decode(PacketBuffer buf) {
            return new NodeDiscoveryPacket(buf.read(CollectionSerializers.list(ClusterNode.class)));
        }
    };
}
