package net.potatocloud.api.property;

/**
 * Represents a property name, type, and default value.
 *
 * @param name the name of the property
 * @param defaultValue the default value of the property
 * @param <T> the type of the value
 */
public record PropertyKey<T>(String name, T defaultValue) {

    /**
     * Creates a new property key.
     *
     * @param name the property name
     * @param defaultValue the property default value
     * @param <T> the property type
     * @return the created property key
     */
    public static <T> PropertyKey<T> of(String name, T defaultValue) {
        return new PropertyKey<>(name, defaultValue);
    }
}