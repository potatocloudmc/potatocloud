package net.potatocloud.common;

import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.property.PropertyKey;

public final class PropertyUtil {

    private PropertyUtil() {
    }

    public static Object parseValue(String value) {
        if (value == null) {
            return null;
        }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {}

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}

        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> void setUnchecked(PropertyHolder holder, PropertyKey<T> key, Object value) {
        holder.set(key, (T) value, false);
    }

    public static void setString(PropertyHolder holder, String name, String value) {
        Object parsed = parseValue(value);
        holder.set(PropertyKey.of(name, parsed), parsed, false);
    }
}
