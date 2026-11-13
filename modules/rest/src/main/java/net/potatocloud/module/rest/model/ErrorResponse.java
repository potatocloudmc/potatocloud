package net.potatocloud.module.rest.model;

public record ErrorResponse(int status, String error, String message) {}
