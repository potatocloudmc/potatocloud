package net.potatocloud.webinterface.api.websocket.handler.group;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.handler.BaseWebSocketHandler;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.broadcast.group.GroupsBroadcastService;

public class GroupsWebSocketHandler extends BaseWebSocketHandler {

    private final GroupsBroadcastService broadcastService;

    public GroupsWebSocketHandler(
            WebSocketSessionManager sessionManager,
            GroupsBroadcastService playerBroadcastService,
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
