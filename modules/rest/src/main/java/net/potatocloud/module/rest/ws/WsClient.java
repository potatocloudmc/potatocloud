package net.potatocloud.module.rest.ws;

import io.javalin.websocket.WsContext;
import net.potatocloud.api.service.LogListener;
import net.potatocloud.module.rest.auth.Scope;
import net.potatocloud.module.rest.config.ApiKey;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WsClient {

    private final WsContext ctx;
    private final ApiKey apiKey;

    private final Set<String> subscribedEvents = ConcurrentHashMap.newKeySet();

    private final Map<String, LogListener> logListeners = new ConcurrentHashMap<>();

    public WsClient(WsContext ctx, ApiKey apiKey) {
        this.ctx = ctx;
        this.apiKey = apiKey;
    }

    public void send(WsMessage message) {
        try {
            ctx.send(message);
        } catch (Exception ignored) {
            // the client is already gone
        }
    }

    public void ok(String message) {
        send(WsMessage.ok(message));
    }

    public void error(String message) {
        send(WsMessage.error(message));
    }

    // checks the scope and tells the client if it is missing
    public boolean allowed(Scope scope) {
        if (apiKey.allows(scope)) {
            return true;
        }

        error("API key is missing the scope " + scope.id());
        return false;
    }

    public Set<String> events() {
        return subscribedEvents;
    }

    public Map<String, LogListener> logListeners() {
        return logListeners;
    }
}
