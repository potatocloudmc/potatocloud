package net.potatocloud.webinterface.api.rest;

import net.potatocloud.webinterface.dto.event.ErrorDto;

public abstract class BaseRestController {

    public abstract void register();

    protected ErrorDto error(String message) {
        return ErrorDto.builder().error(message).build();
    }
}

