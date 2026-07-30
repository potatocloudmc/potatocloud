package net.potatocloud.network.packets.service;

import net.potatocloud.api.service.Service;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceAddPacket(Service service) implements Packet {

    public static final Codec<ServiceAddPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceAddPacket packet, PacketBuffer buf) {
            buf.write(packet.service(), Service.class);
        }

        @Override
        public ServiceAddPacket decode(PacketBuffer buf) {
            return new ServiceAddPacket(buf.read(Service.class));
        }
    };
}
