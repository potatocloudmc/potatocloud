package net.potatocloud.network.codec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SerializerRegistry {

    private SerializerRegistry() {
    }

    private static final Map<Class<?>, TypeSerializer<?>> SERIALIZERS = new ConcurrentHashMap<>();

    static {
        StandardSerializers.registerAll();
    }

    public static <T> void register(Class<T> type, TypeSerializer<T> serializer) {
        if (SERIALIZERS.containsKey(type)) {
            throw new IllegalStateException("Serializer already registered for " + type.getName());
        }
        SERIALIZERS.put(type, serializer);
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeSerializer<T> get(Class<T> type) {
        final TypeSerializer<T> serializer = (TypeSerializer<T>) SERIALIZERS.get(type);
        if (serializer == null) {
            throw new IllegalStateException("No serializer found for " + type.getName());
        }

        return serializer;
    }
}
