package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.webinterface.model.ApiProperty;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PropertyMapper {

    public ApiProperty toApi(PropertyKey<?> property, Object value) {
        return new ApiProperty()
                .name(property.name())
                .value(value)
                .defaultValue(property.defaultValue());
    }

    public List<ApiProperty> toApiList(Map<PropertyKey<?>, Object> properties) {
        return properties.entrySet().stream()
                .map(entry -> new ApiProperty()
                        .name(entry.getKey().name())
                        .value(entry.getValue())
                        .defaultValue(entry.getKey().defaultValue()))
                .toList();
    }

    public Map<PropertyKey<?>, Object> toProperty(ApiProperty apiProperty) {
        return Map.of(new PropertyKey<>(apiProperty.name(), apiProperty.defaultValue()), apiProperty.value());
    }

}
