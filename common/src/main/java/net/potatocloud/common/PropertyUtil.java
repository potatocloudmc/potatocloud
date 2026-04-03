package net.potatocloud.common;

import net.potatocloud.api.property.Property;
import net.potatocloud.api.property.PropertyHolder;

public final class PropertyUtil {

    private PropertyUtil() {
    }

    /**
     * Converts a string into a Property with the right type (needed for property commands both in node and cloud command)
     */
    public static Property<?> stringToProperty(String key, String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Property.ofBoolean(key, Boolean.parseBoolean(value));
        }

        try {
            return Property.ofInteger(key, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {

        }

        try {
            return Property.ofFloat(key, Float.parseFloat(value));
        } catch (NumberFormatException ignored) {

        }

        try {
            return Property.ofDouble(key, Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
        }

        return Property.ofString(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> void setPropertyUnchecked(PropertyHolder holder, Property<?> property) {
        final Property<T> typed = (Property<T>) property;
        holder.setProperty(typed, typed.getValue(), false);
    }
}
