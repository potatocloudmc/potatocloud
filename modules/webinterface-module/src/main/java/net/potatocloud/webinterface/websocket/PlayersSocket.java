package net.potatocloud.webinterface.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.Authenticated;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocket;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.player.CloudPlayerDisconnectEvent;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.webinterface.dto.response.WsEnvelope;
import net.potatocloud.webinterface.openapi.WebSocketDoc;
import net.potatocloud.webinterface.service.PlayerService;

@WebSocketDoc(
        path = "/ws/players",
        summary = "Player updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/players`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/players")
public class PlayersSocket {

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    PlayerService playerService;

    {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(CloudPlayerJoinEvent.class, _ -> this.broadcastPlayersUpdate());
        eventBus.subscribe(CloudPlayerDisconnectEvent.class, _ -> this.broadcastPlayersUpdate());
    }

    @OnOpen
    void onOpen() {
        broadcastPlayersUpdate();
    }

    @Scheduled(every = "5s")
    void periodicPlayersUpdate() {
        broadcastPlayersUpdate();
    }

    void broadcastPlayersUpdate() {
        if (openConnections.listAll().isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("players_update", playerService.findAll())
            );
            openConnections.listAll().forEach(c -> c.sendTextAndAwait(json));
        } catch (Exception e) {
            Log.error("Failed to broadcast players_update", e);
        }
    }
}
