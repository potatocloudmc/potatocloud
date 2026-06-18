package net.potatocloud.webinterface.dto.event;

import lombok.Builder;

@Builder
public record WsEventDto<T>(String type, T data) {
}
