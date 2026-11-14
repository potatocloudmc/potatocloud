package net.potatocloud.module.rest.error;

public record ErrorResponse(int status, String error, String message) {}
