package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.cluster.impl.SimpleClusterNode;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.time.Instant;

public final class ClusterNodeSerializer implements TypeSerializer<ClusterNode> {

    @Override
    public void write(PacketBuffer buffer, ClusterNode node) {
        buffer.writeString(node.name());
        buffer.writeString(node.host());
        buffer.writeVarInt(node.port());
        buffer.write(node.startedAt(), Instant.class);
    }

    @Override
    public ClusterNode read(PacketBuffer buffer) {
        return new SimpleClusterNode(
                buffer.readString(),
                buffer.readString(),
                buffer.readVarInt(),
                buffer.read(Instant.class)
        );
    }
}