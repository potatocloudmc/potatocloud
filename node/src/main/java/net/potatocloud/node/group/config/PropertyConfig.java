package net.potatocloud.node.group.config;

import net.potatocloud.api.property.PropertyKey;

import java.util.Map;

public record PropertyConfig(
        String name,
        Object defaultValue,
        Object value
) {

    public static PropertyConfig from(Map.Entry<PropertyKey<?>, Object> entry) {
        return new PropertyConfig(
                entry.getKey().name(),
                entry.getKey().defaultValue(),
                entry.getValue()
        );
    }

    public Map.Entry<PropertyKey<?>, Object> toEntry() {
        return Map.entry(PropertyKey.of(name, defaultValue), value);
    }
}