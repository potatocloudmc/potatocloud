package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.RequestPacket;

public record RequestServiceLogsPacket(String serviceName) implements RequestPacket {

    public static final Codec<RequestServiceLogsPacket> CODEC = new Codec<>() {

        @Override
        public void encode(RequestServiceLogsPacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
        }

        @Override
        public RequestServiceLogsPacket decode(PacketBuffer buf) {
            return new RequestServiceLogsPacket(buf.readString());
        }
    };
}
