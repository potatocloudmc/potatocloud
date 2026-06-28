package net.potatocloud.api.property;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an object that holds properties.
 */
public interface PropertyHolder {

    /**
     * Gets the name of this property holder.
     *
     * @return the name of this property holder
     */
    String name();

    /**
     * Gets the map of properties by name.
     *
     * @return the property map
     */
    Map<PropertyKey<?>, Object> properties();

    /**
     * Gets the current value of a property.
     *
     * @param key the property key
     * @param <T> the property type
     * @return the stored value or the property's default value
     */
    @SuppressWarnings("unchecked")
    default <T> T get(PropertyKey<T> key) {
        return (T) properties().getOrDefault(key, key.defaultValue());
    }

    /**
     * Gets a property key by its name.
     *
     * @param name the property name
     * @return an optional containing the property key if found
     */
    default Optional<PropertyKey<?>> get(String name) {
        return properties().keySet().stream()
                .filter(key -> key.name().equals(name))
                .findFirst();
    }

    /**
     * Sets a property value and optionally fires {@link PropertyChangedEvent}.
     *
     * @param key       the property key
     * @param value     the new value
     * @param fireEvent {@code true} to fire a PropertyChangedEvent
     * @param <T>       the type of the property value
     */
    default <T> void set(PropertyKey<T> key, T value, boolean fireEvent) {
        final Object oldValue = properties().get(key);

        if (Objects.equals(oldValue, value)) {
            return;
        }

        properties().put(key, value);

        if (fireEvent) {
            CloudAPI.instance().eventBus().publish(new PropertyChangedEvent(name(), key.name(), oldValue, value));
        }
    }

    /**
     * Sets a property value and fires {@link PropertyChangedEvent}.
     *
     * @param key   the property key
     * @param value the new value
     * @param <T>   the type of the property value
     */
    default <T> void set(PropertyKey<T> key, T value) {
        set(key, value, true);
    }

    /**
     * Sets a property value using the default value provided by the key.
     *
     * @param key the property key
     * @param <T> the type of the property value
     */
    default <T> void set(PropertyKey<T> key) {
        set(key, key.defaultValue());
    }

    /**
     * Checks whether the given key exists.
     *
     * @param key the property key
     * @return {@code true} if the property exists
     */
    default boolean hasProperty(PropertyKey<?> key) {
        return properties().containsKey(key);
    }

    /**
     * Removes a property.
     *
     * @param key the property key
     */
    default void removeProperty(PropertyKey<?> key) {
        properties().remove(key);
    }
}
