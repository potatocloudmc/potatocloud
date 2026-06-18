package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.dto.event.WsEventDto;
import net.potatocloud.webinterface.security.AuthService;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public abstract class BaseWebSocketHandler {
    private final WebSocketSessionManager sessionManager;
    private final AuthService authService;
    private final int pingIntervalSeconds;

    public final void configure(WsConfig ws) {
        ws.onConnect(ctx -> {
            if (!authService.authorizeWs(ctx)) {
                ctx.closeSession(4401, "Unauthorized");
                return;
            }

            ctx.enableAutomaticPings(pingIntervalSeconds, TimeUnit.SECONDS);
            sessionManager.add(ctx);

            try {
                onConnect(ctx);
            } catch (Exception exception) {
                sendError(ctx, "Failed to send initial state: " + exception.getMessage());
            }
        });

        ws.onMessage(ctx -> {
            try {
                onMessage(ctx, ctx.message());
            } catch (Exception exception) {
                sendError(ctx, "Error handling message: " + exception.getMessage());
            }
        });

        ws.onClose(ctx -> {
            sessionManager.remove(ctx);
            onClose(ctx);
        });

        ws.onError(ctx -> {
            sessionManager.remove(ctx);
            onError(ctx, ctx.error());
        });
    }

    protected abstract void onConnect(WsContext ctx) throws Exception;

    protected void onMessage(WsContext ctx, String message) {
    }

    protected void onClose(WsContext ctx) {
    }

    protected void onError(WsContext ctx, Throwable error) {
    }

    protected void send(WsContext ctx, String type, Object data) {
        ctx.send(WsEventDto.builder().type(type).data(data).build());
    }

    protected void sendError(WsContext ctx, String message) {
        send(ctx, "error", message);
    }

    protected void broadcast(String type, Object data) {
        sessionManager.broadcast(WsEventDto.builder().type(type).data(data).build());
    }
}

