package net.potatocloud.module.rest.ws;

import io.javalin.config.RoutesConfig;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.config.ApiKey;
import net.potatocloud.module.rest.config.RestConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WsServer {

    private final RestConfig config;

    private final Map<String, WsClient> clients = new ConcurrentHashMap<>();

    private final WsLogs logs;
    private final WsCommands commands;
    private final WsEvents events;

    public WsServer(CloudAPI api, RestConfig config) {
        this.config = config;
        this.logs = new WsLogs(api.serviceManager());
        this.commands = new WsCommands(api.serviceManager());
        this.events = new WsEvents(api.eventBus(), this);
    }

    public void register(RoutesConfig routes) {
        routes.ws("/ws", socket -> {
            socket.onConnect(this::connect);
            socket.onMessage(this::message);
            socket.onClose(ctx -> disconnect(ctx.sessionId()));
            socket.onError(ctx -> disconnect(ctx.sessionId()));
        });
    }

    public void start() {
        events.start();
    }

    public void stop() {
        events.stop();
        clients.keySet().forEach(this::disconnect);
    }

    // sends a cloud event to every client that subscribed to it
    public void sendEvent(String event, Object data) {
        clients.values().stream()
                .filter(client -> client.events().contains(event))
                .forEach(client -> client.send(WsMessage.event(event, data)));
    }

    private void connect(WsConnectContext ctx) {
        String token = ctx.header("X-API-Key");
        if (token == null) {
            token = ctx.queryParam("key");
        }

        final ApiKey apiKey = config.findKey(token);
        if (apiKey == null) {
            ctx.closeSession(1008, "Invalid API key");
            return;
        }

        ctx.enableAutomaticPings();
        clients.put(ctx.sessionId(), new WsClient(ctx, apiKey));
    }

    private void disconnect(String sessionId) {
        final WsClient client = clients.remove(sessionId);

        if (client != null) {
            logs.stopAll(client);
        }
    }

    private void message(WsMessageContext ctx) {
        final WsClient client = clients.get(ctx.sessionId());
        if (client == null) {
            return;
        }

        final WsRequest request;
        try {
            request = ctx.messageAsClass(WsRequest.class);
        } catch (Exception e) {
            client.error("Invalid message");
            return;
        }

        if (request.action() == null) {
            client.error("action is required");
            return;
        }

        switch (request.action()) {
            case "subscribe" -> subscribe(client, request.event());
            case "unsubscribe" -> unsubscribe(client, request.event());
            case "start_logs" -> logs.start(client, request.service());
            case "stop_logs" -> logs.stop(client, request.service());
            case "command" -> commands.run(client, request.service(), request.command());
            default -> client.error("Unknown action " + request.action());
        }
    }

    private void subscribe(WsClient client, String event) {
        if (event == null) {
            client.error("event is required");
            return;
        }

        final Scope scope = scope(event);
        if (scope == null) {
            client.error("Unknown event " + event);
            return;
        }
        if (!client.allowed(scope)) {
            return;
        }

        client.events().add(event);
        client.ok("Subscribed to " + event);
    }

    private void unsubscribe(WsClient client, String event) {
        if (event == null) {
            client.error("event is required");
            return;
        }

        client.events().remove(event);
        client.ok("Unsubscribed from " + event);
    }

    // the scope a client needs to subscribe to an event
    private Scope scope(String event) {
        return switch (event) {
            case "group_created", "group_updated", "group_deleted" -> Scope.GROUP_READ;
            case "service_starting", "service_started", "service_stopping", "service_stopped" -> Scope.SERVICE_READ;
            case "platform_created", "platform_updated", "platform_deleted" -> Scope.PLATFORM_READ;
            case "player_join", "player_disconnect" -> Scope.PLAYER_READ;
            case "node_joined", "node_left" -> Scope.NODE_READ;
            case "property_changed" -> Scope.PROPERTY_READ;
            default -> null;
        };
    }
}
