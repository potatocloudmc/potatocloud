package net.potatocloud.network.packets.service;

import net.potatocloud.api.service.Service;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

public record StartServiceResponsePacket(Service service) implements ResponsePacket {

    public static final Codec<StartServiceResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(StartServiceResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.service(), Service.class);
        }

        @Override
        public StartServiceResponsePacket decode(PacketBuffer buf) {
            return new StartServiceResponsePacket(buf.read(Service.class));
        }
    };
}