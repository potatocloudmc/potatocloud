package net.potatocloud.network.packet.packets.service;

import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;

public record ServiceScreenUnsubscribePacket(String serviceName) implements Packet {

    public static final Codec<ServiceScreenUnsubscribePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceScreenUnsubscribePacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
        }

        @Override
        public ServiceScreenUnsubscribePacket decode(PacketBuffer buf) {
            return new ServiceScreenUnsubscribePacket(buf.readString());
        }
    };
}
