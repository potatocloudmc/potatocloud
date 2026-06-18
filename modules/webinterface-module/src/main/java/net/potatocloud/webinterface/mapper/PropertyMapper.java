package net.potatocloud.webinterface.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import net.potatocloud.api.property.Property;
import net.potatocloud.webinterface.model.ApiProperty;

import java.util.List;

@ApplicationScoped
public class PropertyMapper {

    public ApiProperty toApi(Property<?> property) {
        return new ApiProperty()
                .name(property.name())
                .value(property.value())
                .defaultValue(property.defaultValue());
    }

    public List<Property<?>> toProperty(List<ApiProperty> apiProperties) {
        return apiProperties.stream()
                .map(this::toProperty)
                .toList();
    }

    public Property<?> toProperty(ApiProperty apiProperty) {
        return new Property<>(apiProperty.name(), apiProperty.value(), apiProperty.defaultValue());
    }

}
