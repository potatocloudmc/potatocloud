package net.potatocloud.network.packets.service;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceMemoryUpdatePacket(String serviceName, int usedMemory) implements Packet {

    public static final Codec<ServiceMemoryUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceMemoryUpdatePacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.writeVarInt(packet.usedMemory());
        }

        @Override
        public ServiceMemoryUpdatePacket decode(PacketBuffer buf) {
            return new ServiceMemoryUpdatePacket(buf.readString(), buf.readVarInt());
        }
    };
}