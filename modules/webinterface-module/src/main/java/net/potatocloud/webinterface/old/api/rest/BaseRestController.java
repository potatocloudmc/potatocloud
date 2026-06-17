package net.potatocloud.webinterface.old.api.rest;

import net.potatocloud.webinterface.old.dto.event.ErrorDto;

public abstract class BaseRestController {

    public abstract void register();

    protected ErrorDto error(String message) {
        return ErrorDto.builder().error(message).build();
    }
}

