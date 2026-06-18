package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.broadcast.PlayerBroadcastService;

public class PlayerLiveWebSocketHandler extends BaseWebSocketHandler {

    private final PlayerBroadcastService broadcastService;

    public PlayerLiveWebSocketHandler(
            WebSocketSessionManager sessionManager,
            PlayerBroadcastService playerBroadcastService,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.broadcastService = playerBroadcastService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        broadcastService.register(ctx);
    }

    @Override
    protected void onMessage(WsContext ctx, String message) {
        if (message.contains("\"ping\"")) {
            send(ctx, "pong", null);
        }
    }

    @Override
    protected void onClose(WsContext ctx) {
        broadcastService.unregister(ctx);
    }

    @Override
    protected void onError(WsContext ctx, Throwable error) {
        broadcastService.unregister(ctx);
    }
}

