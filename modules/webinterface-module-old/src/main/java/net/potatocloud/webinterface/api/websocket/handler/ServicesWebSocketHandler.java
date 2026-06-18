package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.broadcast.ServerBroadcastService;

public class ServicesWebSocketHandler extends BaseWebSocketHandler {

    private final ServerBroadcastService serverBroadcastService;

    public ServicesWebSocketHandler(
            ServerBroadcastService statsServerBroadcastService,
            WebSocketSessionManager sessionManager,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.serverBroadcastService = statsServerBroadcastService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        serverBroadcastService.register(ctx);
    }

    @Override
    protected void onClose(WsContext ctx) {
        serverBroadcastService.unregister(ctx);
    }

    @Override
    protected void onError(WsContext ctx, Throwable error) {
        serverBroadcastService.unregister(ctx);
    }
}
