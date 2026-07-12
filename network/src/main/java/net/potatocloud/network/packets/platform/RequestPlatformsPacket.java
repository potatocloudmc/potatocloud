package net.potatocloud.network.packets.platform;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

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