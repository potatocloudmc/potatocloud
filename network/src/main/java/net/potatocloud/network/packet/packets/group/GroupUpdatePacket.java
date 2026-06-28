package net.potatocloud.network.packet.packets.group;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.netty.PacketBuffer;
import net.potatocloud.network.packet.Packet;

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
            buf.writeStringSet(packet.customJvmFlags());
            buf.writeInt(packet.maxPlayers());
            buf.writeInt(packet.maxMemory());
            buf.writeInt(packet.minOnlineCount());
            buf.writeInt(packet.maxOnlineCount());
            buf.writeBoolean(packet.fallback());
            buf.writeInt(packet.startPriority());
            buf.writeInt(packet.startPercentage());
            buf.writeStringSet(packet.templates());
            buf.writePropertyMap(packet.propertyMap());
        }

        @Override
        public GroupUpdatePacket decode(PacketBuffer buf) {
            return new GroupUpdatePacket(
                    buf.readString(),
                    buf.readStringSet(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readStringSet(),
                    buf.readPropertyMap()
            );
        }
    };
}
