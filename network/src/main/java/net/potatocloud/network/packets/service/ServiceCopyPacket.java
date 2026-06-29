package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceCopyPacket(String serviceName, String templateName, String filter) implements Packet {

    public static final Codec<ServiceCopyPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceCopyPacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.writeString(packet.templateName());
            buf.writeString(packet.filter());
        }

        @Override
        public ServiceCopyPacket decode(PacketBuffer buf) {
            return new ServiceCopyPacket(buf.readString(), buf.readString(), buf.readString());
        }
    };
}