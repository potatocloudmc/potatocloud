package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceRemovePacket(String serviceName, String serviceHost, int servicePort) implements Packet {

    public static final Codec<ServiceRemovePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceRemovePacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.writeString(packet.serviceHost());
            buf.writeVarInt(packet.servicePort());
        }

        @Override
        public ServiceRemovePacket decode(PacketBuffer buf) {
            return new ServiceRemovePacket(buf.readString(), buf.readString(), buf.readVarInt());
        }
    };
}