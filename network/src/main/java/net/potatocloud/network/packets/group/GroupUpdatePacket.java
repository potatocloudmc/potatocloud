package net.potatocloud.network.packets.group;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.Map;
import java.util.Set;

public record GroupUpdatePacket(
        String groupName,
        Set<String> customJvmFlags,
        int maxPlayers,
        int maxMemory,
        int minOnlineCount,
        int maxOnlineCount,
        boolean fallback,
        int startPriority,
        int startPercentage,
        Set<String> templates,
        Map<PropertyKey<?>, Object> propertyMap
) implements Packet {

    public static final Codec<GroupUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(GroupUpdatePacket packet, PacketBuffer buf) {
            buf.writeString(packet.groupName());
            buf.write(packet.customJvmFlags(), CollectionSerializers.set(String.class));
            buf.writeVarInt(packet.maxPlayers());
            buf.writeVarInt(packet.maxMemory());
            buf.writeVarInt(packet.minOnlineCount());
            buf.writeVarInt(packet.maxOnlineCount());
            buf.writeBoolean(packet.fallback());
            buf.writeInt(packet.startPriority());
            buf.writeVarInt(packet.startPercentage());
            buf.write(packet.templates(), CollectionSerializers.set(String.class));
            buf.write(packet.propertyMap(), CollectionSerializers.propertyMap());
        }

        @Override
        public GroupUpdatePacket decode(PacketBuffer buf) {
            return new GroupUpdatePacket(
                    buf.readString(),
                    buf.read(CollectionSerializers.set(String.class)),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readVarInt(),
                    buf.read(CollectionSerializers.set(String.class)),
                    buf.read(CollectionSerializers.propertyMap())
            );
        }
    };
}
