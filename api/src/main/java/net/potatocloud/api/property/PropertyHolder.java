package net.potatocloud.api.property;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;

import java.util.Objects;
import java.util.Set;

public interface PropertyHolder {

    Set<Property> getProperties();

    String getPropertyHolderName();

    default Property getProperty(String name) {
        return getProperties().stream()
                .filter(property -> property.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    default void setProperty(Property property, Object value, boolean fireEvent) {
        final Property existingProperty = getProperty(property.getName());
        Object oldValue = null;

        if (existingProperty != null) {
            oldValue = existingProperty.getValue();
            if (Objects.equals(oldValue, value)) {
                return;
            }
            existingProperty.setValue(value);
            property = existingProperty;
        } else {
            property.setValue(value);
            getProperties().add(property);
        }

        if (fireEvent) {
            CloudAPI.getInstance().getEventManager().call(
                    new PropertyChangedEvent(getPropertyHolderName(), property, oldValue, value)
            );
        }
    }

    default void setProperty(Property property, Object value) {
        setProperty(property, value, true);
    }

    default void setProperty(Property property) {
        setProperty(property, property.getValue());
    }

    default boolean hasProperty(String property) {
        return getProperty(property) != null;
    }

    default boolean hasProperty(Property property) {
        return getProperties().contains(property);
    }
}
