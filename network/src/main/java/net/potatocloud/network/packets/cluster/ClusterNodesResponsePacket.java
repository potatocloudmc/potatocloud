package net.potatocloud.network.packets.cluster;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record ClusterNodesResponsePacket(ClusterNode localNode, List<ClusterNode> remoteNodes) implements ResponsePacket {

    public static final Codec<ClusterNodesResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ClusterNodesResponsePacket packet, PacketBuffer buf) {
            buf.writeClusterNode(packet.localNode());
            buf.writeClusterNodeList(packet.remoteNodes());
        }

        @Override
        public ClusterNodesResponsePacket decode(PacketBuffer buf) {
            return new ClusterNodesResponsePacket(buf.readClusterNode(), buf.readClusterNodeList());
        }
    };
}
