package net.potatocloud.webinterface.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocket;
import jakarta.inject.Inject;
import net.potatocloud.webinterface.dto.response.WsEnvelope;
import net.potatocloud.webinterface.openapi.WebSocketDoc;
import net.potatocloud.webinterface.service.NodeService;

@WebSocketDoc(
        path = "/ws/nodes",
        summary = "Nodes updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/nodes`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/nodes")
public class NodesSocket {

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    NodeService nodeService;

    @OnOpen
    void onOpen() {
        broadcastNodesUpdate();
    }

    @Scheduled(every = "5s")
    void periodicNodesUpdate() {
        broadcastNodesUpdate();
    }

    void broadcastNodesUpdate() {
        if (openConnections.listAll().isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("nodes_update", nodeService.clusterNodes())
            );
            openConnections.listAll().forEach(c -> c.sendTextAndAwait(json));
        } catch (Exception e) {
            Log.error("Failed to broadcast nodes_update", e);
        }
    }

}
