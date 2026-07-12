package net.potatocloud.network.packets.player;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.Map;
import java.util.UUID;

public record CloudPlayerUpdatePacket(
        UUID playerUniqueId,
        String connectedProxyName,
        String connectedServiceName,
        Map<PropertyKey<?>, Object> propertyMap
) implements Packet {

    public static final Codec<CloudPlayerUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(CloudPlayerUpdatePacket packet, PacketBuffer buf) {
            buf.write(packet.playerUniqueId(), UUID.class);
            buf.writeString(packet.connectedProxyName());
            buf.writeString(packet.connectedServiceName());
            buf.write(packet.propertyMap(), CollectionSerializers.propertyMap());
        }

        @Override
        public CloudPlayerUpdatePacket decode(PacketBuffer buf) {
            return new CloudPlayerUpdatePacket(
                    buf.read(UUID.class),
                    buf.readString(),
                    buf.readString(),
                    buf.read(CollectionSerializers.propertyMap())
            );
        }
    };
}