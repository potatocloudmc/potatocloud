package net.potatocloud.module.rest.ws;

import com.fasterxml.jackson.annotation.JsonInclude;

// A message sent to a client
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WsMessage(String type, String event, String service, Object data, String message) {

    public static WsMessage event(String event, Object data) {
        return new WsMessage("event", event, null, data, null);
    }

    public static WsMessage log(String service, String line) {
        return new WsMessage("log", null, service, line, null);
    }

    public static WsMessage ok(String message) {
        return new WsMessage("ok", null, null, null, message);
    }

    public static WsMessage error(String message) {
        return new WsMessage("error", null, null, null, message);
    }
}
