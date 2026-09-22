package net.potatocloud.webinterface.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.player.CloudPlayerDisconnectEvent;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.webinterface.dto.response.WsEnvelope;
import net.potatocloud.webinterface.openapi.WebSocketDoc;
import net.potatocloud.webinterface.service.ServerService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocketDoc(
        path = "/ws/services/{serviceName}",
        summary = "Service details updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/services/<serviceName>`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/services/{serviceName}")
public class ServiceDetailsSocket {

    private final Map<String, Set<WebSocketConnection>> connectionsByService = new ConcurrentHashMap<>();
    @Inject
    ObjectMapper objectMapper;
    @Inject
    ServerService serverService;

    {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(ServiceStartedEvent.class, _ -> broadcastAll());
        eventBus.subscribe(ServiceStartingEvent.class, _ -> broadcastAll());
        eventBus.subscribe(ServiceStoppedEvent.class, _ -> broadcastAll());
        eventBus.subscribe(ServiceStoppingEvent.class, _ -> broadcastAll());
        eventBus.subscribe(CloudPlayerJoinEvent.class, _ -> broadcastAll());
        eventBus.subscribe(CloudPlayerDisconnectEvent.class, _ -> broadcastAll());
    }

    @OnOpen
    void onOpen(WebSocketConnection connection) {
        String serviceName = connection.pathParam("serviceName");
        connectionsByService.computeIfAbsent(serviceName, key -> ConcurrentHashMap.newKeySet()).add(connection);
        broadcastServiceUpdate(serviceName);
    }

    @OnClose
    void onClose(WebSocketConnection connection) {
        String serviceName = connection.pathParam("serviceName");
        Set<WebSocketConnection> connections = connectionsByService.get(serviceName);
        if (connections == null) {
            return;
        }
        connections.remove(connection);
        if (connections.isEmpty()) {
            connectionsByService.remove(serviceName);
        }
    }

    @Scheduled(every = "5s")
    void periodicServicesUpdate() {
        broadcastAll();
    }

    private void broadcastAll() {
        connectionsByService.keySet().forEach(this::broadcastServiceUpdate);
    }

    private void broadcastServiceUpdate(String serviceName) {
        Set<WebSocketConnection> connections = connectionsByService.get(serviceName);
        if (connections == null || connections.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("service_details_update", serverService.findByName(serviceName))
            );
            for (WebSocketConnection connection : connections) {
                try {
                    connection.sendTextAndAwait(json);
                } catch (Exception e) {
                    Log.error("Failed to send service_details_update to connection " + connection.id(), e);
                }
            }
        } catch (Exception e) {
            Log.error("Failed to serialize service_details_update for service: " + serviceName, e);
        }
    }
}