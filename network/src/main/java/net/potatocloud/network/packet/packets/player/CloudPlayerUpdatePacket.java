package net.potatocloud.network.packet.packets.player;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;

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
            buf.writeUUID(packet.playerUniqueId());
            buf.writeString(packet.connectedProxyName());
            buf.writeString(packet.connectedServiceName());
            buf.writePropertyMap(packet.propertyMap());
        }

        @Override
        public CloudPlayerUpdatePacket decode(PacketBuffer buf) {
            return new CloudPlayerUpdatePacket(
                    buf.readUUID(),
                    buf.readString(),
                    buf.readString(),
                    buf.readPropertyMap()
            );
        }
    };
}