package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.PlayerService;

public class PlayerWebSocketHandler extends BaseWebSocketHandler {

    private final PlayerService playerService;

    public PlayerWebSocketHandler(
            PlayerService playerService,
            WebSocketSessionManager sessionManager,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.playerService = playerService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        send(ctx, "players_snapshot", playerService.getOnlinePlayers());
    }

    @Override
    protected void onMessage(WsContext ctx, String message) {
        if (message.contains("\"ping\"")) {
            send(ctx, "pong", null);
        }
    }
}

