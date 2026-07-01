package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.impl.GroupImpl;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

public final class GroupSerializer implements TypeSerializer<Group> {

    @Override
    public void write(PacketBuffer buffer, Group group) {
        buffer.writeString(group.name());
        buffer.writeString(group.node().map(ClusterNode::name).orElse(null));
        buffer.writeString(group.platform().name());
        buffer.writeString(group.platformVersion().name());
        buffer.writeString(group.javaCommand());
        buffer.write(group.customJvmFlags(), CollectionSerializers.set(String.class));
        buffer.writeVarInt(group.maxPlayers());
        buffer.writeVarInt(group.maxMemory());
        buffer.writeVarInt(group.minServices());
        buffer.writeVarInt(group.maxServices());
        buffer.writeBoolean(group.staticServices());
        buffer.writeBoolean(group.fallback());
        buffer.writeInt(group.startPriority());
        buffer.writeVarInt(group.startPercentage());
        buffer.write(group.templates(), CollectionSerializers.set(String.class));
        buffer.write(group.properties(), CollectionSerializers.propertyMap());
    }

    @Override
    public Group read(PacketBuffer buffer) {
        return new GroupImpl(
                buffer.readString(),
                buffer.readString(),
                buffer.readString(),
                buffer.readString(),
                buffer.readString(),
                buffer.read(CollectionSerializers.set(String.class)),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readVarInt(),
                buffer.read(CollectionSerializers.set(String.class)),
                buffer.read(CollectionSerializers.propertyMap())
        );
    }
}
