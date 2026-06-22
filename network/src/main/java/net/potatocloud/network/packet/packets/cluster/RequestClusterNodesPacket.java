package net.potatocloud.network.packet.packets.cluster;

import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.request.RequestPacket;

public record RequestClusterNodesPacket() implements RequestPacket {

    public static final Codec<RequestClusterNodesPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestClusterNodesPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestClusterNodesPacket decode(PacketBuffer buf) {
            return new RequestClusterNodesPacket();
        }
    };
}
