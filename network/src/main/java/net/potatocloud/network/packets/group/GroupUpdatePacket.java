package net.potatocloud.network.packets.group;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.template.Template;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record GroupUpdatePacket(
        String groupName,
        String nodeName,
        String platformName,
        String platformVersionName,
        String javaCommand,
        Set<String> customJvmFlags,
        int maxPlayers,
        int maxMemory,
        int minOnlineCount,
        int maxOnlineCount,
        boolean fallback,
        int startPriority,
        int startPercentage,
        List<Template> templates,
        Map<PropertyKey<?>, Object> propertyMap
) implements Packet {

    public static GroupUpdatePacket of(Group group) {
        return new GroupUpdatePacket(
                group.name(),
                group.node().map(ClusterNode::name).orElse(null),
                group.platform().name(),
                group.platformVersion().name(),
                group.javaCommand(),
                group.customJvmFlags(),
                group.maxPlayers(),
                group.maxMemory(),
                group.minServices(),
                group.maxServices(),
                group.fallback(),
                group.startPriority(),
                group.startPercentage(),
                group.templates(),
                group.properties()
        );
    }

    public static final Codec<GroupUpdatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(GroupUpdatePacket packet, PacketBuffer buf) {
            buf.writeString(packet.groupName());
            buf.writeString(packet.nodeName());
            buf.writeString(packet.platformName());
            buf.writeString(packet.platformVersionName());
            buf.writeString(packet.javaCommand());
            buf.write(packet.customJvmFlags(), CollectionSerializers.set(String.class));
            buf.writeVarInt(packet.maxPlayers());
            buf.writeVarInt(packet.maxMemory());
            buf.writeVarInt(packet.minOnlineCount());
            buf.writeVarInt(packet.maxOnlineCount());
            buf.writeBoolean(packet.fallback());
            buf.writeInt(packet.startPriority());
            buf.writeVarInt(packet.startPercentage());
            buf.write(packet.templates(), CollectionSerializers.list(Template.class));
            buf.write(packet.propertyMap(), CollectionSerializers.propertyMap());
        }

        @Override
        public GroupUpdatePacket decode(PacketBuffer buf) {
            return new GroupUpdatePacket(
                    buf.readString(),
                    buf.readString(),
                    buf.readString(),
                    buf.readString(),
                    buf.readString(),
                    buf.read(CollectionSerializers.set(String.class)),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readVarInt(),
                    buf.read(CollectionSerializers.list(Template.class)),
                    buf.read(CollectionSerializers.propertyMap())
            );
        }
    };
}
