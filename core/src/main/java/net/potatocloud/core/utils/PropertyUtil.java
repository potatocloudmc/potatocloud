package net.potatocloud.core.utils;

import lombok.experimental.UtilityClass;
import net.potatocloud.api.property.Property;

@UtilityClass
public class PropertyUtil {

    /**
     * Converts a string into a Property with the right type (needed for property commands both in node and cloud command)
     */
    public Property<?> stringToProperty(String key, String value) {
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

        return Property.ofString(key, value);
    }
}
