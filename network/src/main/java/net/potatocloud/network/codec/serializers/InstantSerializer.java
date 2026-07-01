package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.time.Instant;

public final class InstantSerializer implements TypeSerializer<Instant> {

    @Override
    public void write(PacketBuffer buffer, Instant value) {
        buffer.writeLong(value.toEpochMilli());
    }

    @Override
    public Instant read(PacketBuffer buffer) {
        return Instant.ofEpochMilli(buffer.readLong());
    }
}