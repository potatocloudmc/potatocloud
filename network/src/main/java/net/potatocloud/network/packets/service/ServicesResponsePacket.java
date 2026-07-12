package net.potatocloud.network.packets.service;

import net.potatocloud.api.service.Service;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record ServicesResponsePacket(List<Service> services) implements ResponsePacket {

    public static final Codec<ServicesResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServicesResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.services(), CollectionSerializers.list(Service.class));
        }

        @Override
        public ServicesResponsePacket decode(PacketBuffer buf) {
            return new ServicesResponsePacket(buf.read(CollectionSerializers.list(Service.class)));
        }
    };
}
