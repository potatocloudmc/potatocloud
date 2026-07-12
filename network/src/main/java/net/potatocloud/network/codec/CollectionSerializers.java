package net.potatocloud.network.codec;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.serializers.ListSerializer;
import net.potatocloud.network.codec.serializers.MapSerializer;
import net.potatocloud.network.codec.serializers.SetSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionSerializers {

    private CollectionSerializers() {
    }

    public static <T> TypeSerializer<List<T>> list(Class<T> type) {
        return new ListSerializer<>(SerializerRegistry.get(type));
    }

    public static <T> TypeSerializer<Set<T>> set(Class<T> type) {
        return new SetSerializer<>(SerializerRegistry.get(type));
    }

    public static <K, V> TypeSerializer<Map<K, V>> map(Class<K> key, Class<V> value) {
        return new MapSerializer<>(SerializerRegistry.get(key), SerializerRegistry.get(value));
    }

    @SuppressWarnings("unchecked")
    public static TypeSerializer<Map<PropertyKey<?>, Object>> propertyMap() {
        return (TypeSerializer<Map<PropertyKey<?>, Object>>) (TypeSerializer<?>) map(PropertyKey.class, Object.class);
    }
}