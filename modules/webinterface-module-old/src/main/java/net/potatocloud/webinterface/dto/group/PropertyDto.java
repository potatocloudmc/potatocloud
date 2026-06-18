package net.potatocloud.webinterface.dto.group;

import lombok.Builder;
import net.potatocloud.api.property.Property;

@Builder
public record PropertyDto(String name, Object value, Object defaultValue) {
    public static PropertyDto from(Property<?> property) {
        return PropertyDto.builder()
                .name(property.name())
                .value(property.value())
                .defaultValue(property.defaultValue())
                .build();
    }
}

