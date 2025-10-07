package net.potatocloud.api.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Property<T> {

    private final String name;
    private final T defaultValue;

    @Setter
    private T value;

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
