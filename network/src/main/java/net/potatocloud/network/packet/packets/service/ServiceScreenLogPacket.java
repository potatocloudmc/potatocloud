package net.potatocloud.network.packet.packets.service;

import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;

public record ServiceScreenLogPacket(String serviceName, String line) implements Packet {

    public static final Codec<ServiceScreenLogPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceScreenLogPacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.writeString(packet.line());
        }

        @Override
        public ServiceScreenLogPacket decode(PacketBuffer buf) {
            return new ServiceScreenLogPacket(buf.readString(), buf.readString());
        }
    };
}
