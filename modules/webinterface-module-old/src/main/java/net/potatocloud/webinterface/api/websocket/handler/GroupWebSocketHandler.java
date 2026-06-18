package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.GroupService;

public class GroupWebSocketHandler extends BaseWebSocketHandler {

    private final GroupService groupService;

    public GroupWebSocketHandler(
            GroupService groupService,
            WebSocketSessionManager sessionManager,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.groupService = groupService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        send(ctx, "groups_snapshot", groupService.getAllGroups());
    }

    @Override
    protected void onMessage(WsContext ctx, String message) {
        if (message.contains("\"ping\"")) {
            send(ctx, "pong", null);
        }
    }
}

