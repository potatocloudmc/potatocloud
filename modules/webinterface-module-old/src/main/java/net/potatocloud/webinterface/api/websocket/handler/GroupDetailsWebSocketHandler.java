package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.GroupService;
import net.potatocloud.webinterface.service.broadcast.group.GroupDetailsBroadcastService;

public class GroupDetailsWebSocketHandler extends BaseWebSocketHandler {

    private final GroupService groupService;
    private final GroupDetailsBroadcastService broadcastService;

    public GroupDetailsWebSocketHandler(
            GroupService groupService,
            GroupDetailsBroadcastService broadcastService,
            WebSocketSessionManager sessionManager,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.groupService = groupService;
        this.broadcastService = broadcastService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        String groupName = ctx.queryParam("group");
        if (groupName == null || groupName.isBlank()) {
            ctx.closeSession(1008, "group parameter required");
            return;
        }

        if (groupService.getGroupByName(groupName) == null) {
            sendError(ctx, "Group not found: " + groupName);
            return;
        }

        broadcastService.register(groupName, ctx);
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

