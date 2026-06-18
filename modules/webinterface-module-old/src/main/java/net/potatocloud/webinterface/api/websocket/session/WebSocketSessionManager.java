package net.potatocloud.webinterface.api.websocket.session;

import io.javalin.websocket.WsContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketSessionManager {

    private final Map<String, WsContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<WsContext>> sessionsByChannel = new ConcurrentHashMap<>();

    public void add(WsContext ctx) {
        sessions.put(ctx.sessionId(), ctx);
    }

    public void addToChannel(String channel, WsContext ctx) {
        add(ctx);
        sessionsByChannel.computeIfAbsent(channel, key -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    public void remove(WsContext ctx) {
        sessions.remove(ctx.sessionId());
        sessionsByChannel.values().forEach(set -> set.remove(ctx));
    }

    public Collection<WsContext> all() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    public Set<WsContext> byChannel(String channel) {
        return sessionsByChannel.getOrDefault(channel, Collections.emptySet());
    }

    public int count() {
        return sessions.size();
    }

    public void broadcast(Object message) {
        sessions.values().forEach(ctx -> {
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(message);
                }
            } catch (Exception ignored) {
            }
        });
    }

    public void closeAll(String reason) {
        sessions.values().forEach(ctx -> {
            try {
                ctx.closeSession(1001, reason);
            } catch (Exception ignored) {
            }
        });
        sessions.clear();
        sessionsByChannel.clear();
    }
}

