package net.potatocloud.network.packets.service;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.Map;

public record ServiceUpdatePacket(
        String serviceName,
        String state,
        int maxPlayers,
        Map<PropertyKey<?>, Object> propertyMap
) implements Packet {

    public static final Codec<ServiceUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceUpdatePacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.writeString(packet.state());
            buf.writeInt(packet.maxPlayers());
            buf.writePropertyMap(packet.propertyMap());
        }

        @Override
        public ServiceUpdatePacket decode(PacketBuffer buf) {
            return new ServiceUpdatePacket(
                    buf.readString(),
                    buf.readString(),
                    buf.readInt(),
                    buf.readPropertyMap()
            );
        }
    };
}