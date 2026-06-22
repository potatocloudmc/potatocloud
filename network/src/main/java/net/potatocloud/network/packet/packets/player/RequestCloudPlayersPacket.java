package net.potatocloud.network.packet.packets.player;

import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.request.RequestPacket;

public record RequestCloudPlayersPacket() implements RequestPacket {

    public static final Codec<RequestCloudPlayersPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestCloudPlayersPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestCloudPlayersPacket decode(PacketBuffer buf) {
            return new RequestCloudPlayersPacket();
        }
    };
}