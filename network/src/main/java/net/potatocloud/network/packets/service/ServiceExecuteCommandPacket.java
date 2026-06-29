package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceExecuteCommandPacket(String serviceName, String command) implements Packet {

    public static final Codec<ServiceExecuteCommandPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceExecuteCommandPacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.writeString(packet.command());
        }

        @Override
        public ServiceExecuteCommandPacket decode(PacketBuffer buf) {
            return new ServiceExecuteCommandPacket(buf.readString(), buf.readString());
        }
    };
}