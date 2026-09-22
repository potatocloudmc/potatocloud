package net.potatocloud.webinterface.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import io.quarkus.websockets.next.*;
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
import net.potatocloud.webinterface.service.GroupService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocketDoc(
        path = "/ws/groups/{groupName}",
        summary = "Groups details updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/groups/<groupName>`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/groups/{groupName}")
public class GroupDetailsSocket {

    private final Map<String, Set<WebSocketConnection>> connectionsByGroup = new ConcurrentHashMap<>();

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    GroupService groupService;

    {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(ServiceStartingEvent.class, _ -> this.broadcastAll());
        eventBus.subscribe(ServiceStoppedEvent.class, _ -> this.broadcastAll());
        eventBus.subscribe(ServiceStoppingEvent.class, _ -> this.broadcastAll());
        eventBus.subscribe(ServiceStartedEvent.class, _ -> this.broadcastAll());
        eventBus.subscribe(CloudPlayerJoinEvent.class, _ -> this.broadcastAll());
        eventBus.subscribe(CloudPlayerDisconnectEvent.class, _ -> this.broadcastAll());
    }

    @OnOpen
    void onOpen(WebSocketConnection connection) {
        String groupName = connection.pathParam("groupName");
        connectionsByGroup.computeIfAbsent(groupName, key -> ConcurrentHashMap.newKeySet()).add(connection);
        broadcast(groupName);
    }

    @OnClose
    void onClose(WebSocketConnection connection) {
        String groupName = connection.pathParam("groupName");
        Set<WebSocketConnection> connections = connectionsByGroup.get(groupName);
        if (connections == null) {
            return;
        }
        connections.remove(connection);
        if (connections.isEmpty()) {
            connectionsByGroup.remove(groupName);
        }
    }

    @Scheduled(every = "5s")
    void periodicPlayersUpdate() {
        broadcastAll();
    }

    private void broadcastAll() {
        connectionsByGroup.keySet().forEach(this::broadcast);
    }


    void broadcast(String groupName) {
        Set<WebSocketConnection> connections = connectionsByGroup.get(groupName);
        if (connections == null || connections.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("group_details_update", groupService.findByName(groupName))
            );
            for (WebSocketConnection connection : connections) {
                try {
                    connection.sendTextAndAwait(json);
                } catch (Exception e) {
                    Log.error("Failed to send group_details_update to connection " + connection.id(), e);
                }
            }
        } catch (Exception e) {
            Log.error("Failed to serialize group_details_update for group: " + groupName, e);
        }
    }

}
