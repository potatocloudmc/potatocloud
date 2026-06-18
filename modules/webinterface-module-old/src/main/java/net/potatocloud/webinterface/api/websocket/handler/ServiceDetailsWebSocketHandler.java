package net.potatocloud.webinterface.api.websocket.handler;

import io.javalin.websocket.WsContext;
import net.potatocloud.webinterface.api.websocket.session.WebSocketSessionManager;
import net.potatocloud.webinterface.security.AuthService;
import net.potatocloud.webinterface.service.ServerService;
import net.potatocloud.webinterface.service.broadcast.ServiceDetailsBroadcastService;

public class ServiceDetailsWebSocketHandler extends BaseWebSocketHandler {

    private final ServerService serverService;
    private final ServiceDetailsBroadcastService serviceDetailsBroadcastService;

    public ServiceDetailsWebSocketHandler(
            ServerService serverService,
            ServiceDetailsBroadcastService serviceDetailsBroadcastService,
            WebSocketSessionManager sessionManager,
            AuthService authService,
            int pingIntervalSeconds
    ) {
        super(sessionManager, authService, pingIntervalSeconds);
        this.serverService = serverService;
        this.serviceDetailsBroadcastService = serviceDetailsBroadcastService;
    }

    @Override
    protected void onConnect(WsContext ctx) {
        String service = ctx.queryParam("name");
        if (service == null || service.isBlank()) {
            ctx.closeSession(1008, "name parameter required");
            return;
        }

        if (serverService.getService(service) == null) {
            sendError(ctx, "Service not found: " + service);
            return;
        }

        serviceDetailsBroadcastService.register(service, ctx);
    }

    @Override
    protected void onClose(WsContext ctx) {
        serviceDetailsBroadcastService.unregister(ctx);
    }

    @Override
    protected void onError(WsContext ctx, Throwable error) {
        serviceDetailsBroadcastService.unregister(ctx);
    }

}
