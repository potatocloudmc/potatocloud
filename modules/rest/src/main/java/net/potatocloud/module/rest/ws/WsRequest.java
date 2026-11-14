package net.potatocloud.module.rest.ws;

// A message sent by a client
public record WsRequest(String action, String event, String service, String command) {}
