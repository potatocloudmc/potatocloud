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
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.webinterface.dto.response.WsEnvelope;
import net.potatocloud.webinterface.openapi.WebSocketDoc;
import net.potatocloud.webinterface.service.StatsService;

@WebSocketDoc(
        path = "/ws/service-stats",
        summary = "Service stats updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/service-stats`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/service-stats")
public class ServiceStatsSocket {

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    StatsService statsService;

    {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(ServiceStartedEvent.class, _ -> this.broadcastServiceStatsUpdate());
        eventBus.subscribe(ServiceStartingEvent.class, _ -> this.broadcastServiceStatsUpdate());
        eventBus.subscribe(ServiceStoppedEvent.class, _ -> this.broadcastServiceStatsUpdate());
        eventBus.subscribe(ServiceStoppingEvent.class, _ -> this.broadcastServiceStatsUpdate());
    }

    @OnOpen
    void onOpen() {
        broadcastServiceStatsUpdate();
    }

    @Scheduled(every = "5s")
    void periodicServicesUpdate() {
        broadcastServiceStatsUpdate();
    }

    void broadcastServiceStatsUpdate() {
        if (openConnections.listAll().isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("service_stats_update", statsService.serviceStats())
            );
            openConnections.listAll().forEach(c -> c.sendTextAndAwait(json));
        } catch (Exception e) {
            Log.error("Failed to broadcast service_stats_update", e);
        }
    }

}
