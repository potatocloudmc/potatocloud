package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceScreenSubscribePacket(String serviceName) implements Packet {

    public static final Codec<ServiceScreenSubscribePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceScreenSubscribePacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
        }

        @Override
        public ServiceScreenSubscribePacket decode(PacketBuffer buf) {
            return new ServiceScreenSubscribePacket(buf.readString());
        }
    };
}
