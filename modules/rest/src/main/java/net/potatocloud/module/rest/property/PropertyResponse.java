package net.potatocloud.module.rest.property;

import net.potatocloud.api.property.PropertyHolder;

import java.util.Map;
import java.util.TreeMap;

public record PropertyResponse(String name, Object value) {

    public static Map<String, Object> map(PropertyHolder holder) {
        final Map<String, Object> map = new TreeMap<>();
        holder.properties().forEach((key, value) -> map.put(key.name(), value));
        return map;
    }
}
