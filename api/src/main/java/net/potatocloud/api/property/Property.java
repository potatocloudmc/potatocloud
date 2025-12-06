package net.potatocloud.api.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Property<T> {

    /**
     * The name of the property.
     */
    private final String name;

    /**
     * The default value of the property.
     */
    private final T defaultValue;

    /**
     * The value of the property.
     */
    @Setter
    private T value;

    /**
     * Sets the value of the property using an object.
     *
     * @param value the value to set
     */
    @SuppressWarnings("unchecked")
    public void setValueObject(Object value) {
        this.value = (T) value;
    }

    /**
     * Gets the current value of the property. Returns the default value if the current value is {@code null}
     *
     * @return the property value
     */
    public T getValue() {
        return value != null ? value : defaultValue;
    }

    public static Property<String> ofString(String name, String defaultValue) {
        return new Property<>(name, defaultValue, defaultValue);
    }

    public static Property<Integer> ofInteger(String name, int defaultValue) {
        return new Property<>(name, defaultValue, defaultValue);
    }

    public static Property<Boolean> ofBoolean(String name, boolean defaultValue) {
        return new Property<>(name, defaultValue, defaultValue);
    }

    public static Property<Float> ofFloat(String name, float defaultValue) {
        return new Property<>(name, defaultValue, defaultValue);
    }

    public static <T> Property<T> of(String name, T defaultValue, T value) {
        return new Property<>(name, defaultValue, value);
    }
}
