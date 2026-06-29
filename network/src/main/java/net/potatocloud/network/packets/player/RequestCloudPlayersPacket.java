package net.potatocloud.network.packets.player;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

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