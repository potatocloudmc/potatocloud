package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

public final class ListSerializer<T> implements TypeSerializer<List<T>> {

    private final TypeSerializer<T> serializer;

    public ListSerializer(TypeSerializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void write(PacketBuffer buffer, List<T> value) {
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
    public List<T> read(PacketBuffer buffer) {
        final int size = buffer.readVarInt();
        if (size == -1) {
            return new ArrayList<>();
        }

        final List<T> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(serializer.read(buffer));
        }

        return list;
    }
}