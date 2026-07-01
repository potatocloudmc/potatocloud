package net.potatocloud.network.packets.cluster;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record ClusterNodesResponsePacket(ClusterNode localNode, List<ClusterNode> remoteNodes) implements ResponsePacket {

    public static final Codec<ClusterNodesResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ClusterNodesResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.localNode(), ClusterNode.class);
            buf.write(packet.remoteNodes(), CollectionSerializers.list(ClusterNode.class));
        }

        @Override
        public ClusterNodesResponsePacket decode(PacketBuffer buf) {
            return new ClusterNodesResponsePacket(buf.read(ClusterNode.class), buf.read(CollectionSerializers.list(ClusterNode.class)));
        }
    };
}
