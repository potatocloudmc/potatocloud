package net.potatocloud.api.property;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;

import java.util.List;
import java.util.Map;

public interface PropertyHolder {

    Map<String, Property<?>> getPropertyMap();

    default List<Property<?>> getProperties() {
        return getPropertyMap().values().stream().toList();
    }

    String getPropertyHolderName();

    default <T> Property<T> getProperty(String name) {
        return (Property<T>) getPropertyMap().get(name);
    }

    default <T> Property<T> getProperty(Property<T> property) {
        return getProperty(property.getName());
    }

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

    default boolean hasProperty(String name) {
        return getPropertyMap().containsKey(name);
    }

    default boolean hasProperty(Property<?> property) {
        return getPropertyMap().containsValue(property);
    }
}
