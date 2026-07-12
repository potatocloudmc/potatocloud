package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.util.HashSet;
import java.util.Set;

public final class SetSerializer<T> implements TypeSerializer<Set<T>> {

    private final TypeSerializer<T> serializer;

    public SetSerializer(TypeSerializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void write(PacketBuffer buffer, Set<T> value) {
        if (value == null) {
            buffer.writeVarInt(-1);
            return;
        }

        buffer.writeVarInt(value.size());

        for (T element : value) {
            serializer.write(buffer, element);
        }
    }

    @Override
    public Set<T> read(PacketBuffer buffer) {
        final int size = buffer.readVarInt();
        if (size == -1) {
            return new HashSet<>();
        }

        final Set<T> set = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            set.add(serializer.read(buffer));
        }

        return set;
    }
}