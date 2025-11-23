package net.potatocloud.api.property;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;

import java.util.List;
import java.util.Map;

public interface PropertyHolder {

    /**
     * Gets the map of properties by name.
     *
     * @return the property map
     */
    Map<String, Property<?>> getPropertyMap();

    /**
     * Gets the list of all properties of this holder.
     *
     * @return the list of all properties of this holder.
     */
    default List<Property<?>> getProperties() {
        return getPropertyMap().values().stream().toList();
    }

    /**
     * Gets the name of this property holder.
     *
     * @return the name of this property holder.
     */
    String getPropertyHolderName();

    /**
     * Gets a property by name.
     *
     * @param name the name of the property
     * @param <T>  the type of the property value
     * @return the property
     */
    default <T> Property<T> getProperty(String name) {
        return (Property<T>) getPropertyMap().get(name);
    }

    /**
     * Gets a property by reference.
     *
     * @param property the property to get
     * @param <T>      the type of the property value
     * @return the property
     */
    default <T> Property<T> getProperty(Property<T> property) {
        return getProperty(property.getName());
    }

    /**
     * Sets a property value and optionally fires {@link PropertyChangedEvent}.
     *
     * @param property  the property to set
     * @param value     the new value
     * @param fireEvent {@code true} to fire a PropertyChangedEvent, {@code false} to not
     * @param <T>       the type of the property value
     */
    default <T> void setProperty(Property<T> property, T value, boolean fireEvent) {
        final Property<T> existing = getProperty(property.getName());
        Object oldValue = null;

        if (existing != null) {
            oldValue = existing.getValue();
            if (oldValue.equals(value)) {
                return;
            }

            existing.setValue(value);
            property = existing;
        } else {
            property.setValue(value);
            getPropertyMap().put(property.getName(), property);
        }

        if (fireEvent) {
            CloudAPI.getInstance().getEventManager().call(
                    new PropertyChangedEvent(getPropertyHolderName(), property, oldValue, value)
            );
        }
    }

    default <T> void setProperty(Property<T> property, T value) {
        setProperty(property, value, true);
    }

    default <T> void setProperty(Property<T> property) {
        setProperty(property, property.getValue());
    }

    /**
     * Checks whether a property with the given name exists.
     *
     * @param name the name of the property
     * @return {@code true} if a property with the given name exists, otherwise {@code false}
     */
    default boolean hasProperty(String name) {
        return getPropertyMap().containsKey(name);
    }

    /**
     * Checks whether the given property exists.
     *
     * @param property the property
     * @return {@code true} if the property exists, otherwise {@code false}
     */
    default boolean hasProperty(Property<?> property) {
        return getPropertyMap().containsValue(property);
    }

}
