package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceStartedPacket(String serviceName) implements Packet {

    public static final Codec<ServiceStartedPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceStartedPacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
        }

        @Override
        public ServiceStartedPacket decode(PacketBuffer buf) {
            return new ServiceStartedPacket(buf.readString());
        }
    };
}