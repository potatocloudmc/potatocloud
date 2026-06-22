package net.potatocloud.network.packet.packets.platform;

import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.request.RequestPacket;

public record RequestPlatformsPacket() implements RequestPacket {

    public static final Codec<RequestPlatformsPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestPlatformsPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestPlatformsPacket decode(PacketBuffer buf) {
            return new RequestPlatformsPacket();
        }
    };
}