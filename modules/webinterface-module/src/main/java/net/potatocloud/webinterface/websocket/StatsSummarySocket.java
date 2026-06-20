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
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.webinterface.dto.response.WsEnvelope;
import net.potatocloud.webinterface.openapi.WebSocketDoc;
import net.potatocloud.webinterface.service.StatsService;

@WebSocketDoc(
        path = "/ws/stats/summary",
        summary = "Summary stats updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/stats/summary`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/stats/summary")
public class StatsSummarySocket {

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    StatsService statsService;

    {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(ServiceStartedEvent.class, _ -> this.broadcast());
        eventBus.subscribe(ServiceStartingEvent.class, _ -> this.broadcast());
        eventBus.subscribe(ServiceStoppedEvent.class, _ -> this.broadcast());
        eventBus.subscribe(ServiceStoppingEvent.class, _ -> this.broadcast());
        eventBus.subscribe(CloudPlayerDisconnectEvent.class, _ -> this.broadcast());
        eventBus.subscribe(CloudPlayerJoinEvent.class, _ -> this.broadcast());
    }

    @OnOpen
    void onOpen() {
        broadcast();
    }

    @Scheduled(every = "5s")
    void periodicServicesUpdate() {
        broadcast();
    }

    void broadcast() {
        if (openConnections.listAll().isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("stats_summary_update", statsService.statsSummary())
            );
            openConnections.listAll().forEach(c -> c.sendTextAndAwait(json));
        } catch (Exception e) {
            Log.error("Failed to broadcast stats_summary_update", e);
        }
    }

}
