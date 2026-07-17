package net.potatocloud.node.platform.cache;

import java.util.HashMap;
import java.util.Map;

public final class CacheRegistry {

    private final Map<String, CacheBuilder> builders = new HashMap<>();

    public CacheRegistry() {
        register("paper", new PaperCacheBuilder());
    }

    public void register(String name, CacheBuilder builder) {
        builders.put(name.toLowerCase(), builder);
    }

    public CacheBuilder get(String name) {
        final CacheBuilder builder = builders.get(name.toLowerCase());

        if (builder == null) {
            throw new IllegalStateException("Unknown CacheBuilder: " + name);
        }

        return builder;
    }
}
