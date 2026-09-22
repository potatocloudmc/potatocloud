package net.potatocloud.webinterface.dto.response;

public record WsEnvelope<T>(String type, T data) {
}