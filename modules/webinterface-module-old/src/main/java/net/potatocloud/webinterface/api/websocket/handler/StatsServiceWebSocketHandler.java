package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.broadcast.stats.StatsServicesBroadcastService;

public class StatsServiceWebSocketHandler extends BaseWebSocketHandler {

    private final StatsServicesBroadcastService screenLogBroadcastService;

    public StatsServiceWebSocketHandler(
            StatsServicesBroadcastService statsServicesBroadcastService,
            WebSocketSessionManager sessionManager,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.screenLogBroadcastService = statsServicesBroadcastService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        screenLogBroadcastService.register(ctx);
    }

    @Override
    protected void onClose(WsContext ctx) {
        screenLogBroadcastService.unregister(ctx);
    }

    @Override
    protected void onError(WsContext ctx, Throwable error) {
        screenLogBroadcastService.unregister(ctx);
    }
}

