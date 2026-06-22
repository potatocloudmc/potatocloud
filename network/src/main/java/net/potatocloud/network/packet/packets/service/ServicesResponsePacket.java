package net.potatocloud.network.packet.packets.service;

import net.potatocloud.api.service.Service;
import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;
import net.potatocloud.network.packet.request.ResponsePacket;

import java.util.List;

public record ServicesResponsePacket(List<Service> services) implements ResponsePacket {

    public static final Codec<ServicesResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServicesResponsePacket packet, PacketBuffer buf) {
            buf.writeServiceList(packet.services());
        }

        @Override
        public ServicesResponsePacket decode(PacketBuffer buf) {
            return new ServicesResponsePacket(buf.readServiceList());
        }
    };
}
