package net.potatocloud.network.packets.cluster;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

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
