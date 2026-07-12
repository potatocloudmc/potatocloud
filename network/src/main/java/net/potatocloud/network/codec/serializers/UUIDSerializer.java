package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.util.UUID;

public final class UUIDSerializer implements TypeSerializer<UUID> {

    @Override
    public void write(PacketBuffer buffer, UUID value) {
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());
    }

    @Override
    public UUID read(PacketBuffer buffer) {
        return new UUID(buffer.readLong(), buffer.readLong());
    }
}