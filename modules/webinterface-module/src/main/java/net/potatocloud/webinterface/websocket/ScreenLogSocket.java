package net.potatocloud.webinterface.websocket;

import io.quarkus.security.Authenticated;
import io.quarkus.websockets.next.*;
import jakarta.inject.Inject;
import net.potatocloud.webinterface.openapi.WebSocketDoc;
import net.potatocloud.webinterface.service.ScreenLogService;
import net.potatocloud.webinterface.service.ServerService;

@WebSocketDoc(
        path = "/ws/screens/{screenName]",
        summary = "Screen live updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/screens/<screenName>`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/screens/{screenName}")
public class ScreenLogSocket {

    @Inject
    ScreenLogService screenLogService;

    @Inject
    ServerService serverService;

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        String screenName = connection.pathParam("screenName");
        screenLogService.register(screenName, connection);
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        String screenName = connection.pathParam("screenName");
        screenLogService.unregister(screenName, connection);
    }

    @OnTextMessage
    public void onTextMessage(WebSocketConnection connection, String message) {
        String screenName = connection.pathParam("screenName");
        if (message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1);
        }
        serverService.execute(screenName, message);
    }

}
