package net.potatocloud.network.packets.group;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record RequestGroupsPacket() implements RequestPacket {

    public static final Codec<RequestGroupsPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestGroupsPacket packet, PacketBuffer buf) {
        }

        @Override
        public RequestGroupsPacket decode(PacketBuffer buf) {
            return new RequestGroupsPacket();
        }
    };
}

