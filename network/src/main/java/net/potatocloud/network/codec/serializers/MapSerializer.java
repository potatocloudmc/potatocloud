package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.util.HashMap;
import java.util.Map;

public final class MapSerializer<K, V> implements TypeSerializer<Map<K, V>> {

    private final TypeSerializer<K> keySerializer;
    private final TypeSerializer<V> valueSerializer;

    public MapSerializer(TypeSerializer<K> keySerializer, TypeSerializer<V> valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public void write(PacketBuffer buffer, Map<K, V> map) {
        if (map == null) {
            buffer.writeVarInt(-1);
            return;
        }

        buffer.writeVarInt(map.size());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            keySerializer.write(buffer, entry.getKey());
            valueSerializer.write(buffer, entry.getValue());
        }
    }

    @Override
    public Map<K, V> read(PacketBuffer buffer) {
        final int size = buffer.readVarInt();
        if (size == -1) {
            return new HashMap<>();
        }

        final Map<K, V> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            final K key = keySerializer.read(buffer);
            final V value = valueSerializer.read(buffer);

            map.put(key, value);
        }

        return map;
    }
}