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
import net.potatocloud.webinterface.service.ServerService;

@WebSocketDoc(
        path = "/ws/services",
        summary = "Services updates (WebSocket)",
        description = """
                Connect via `wss://<host>/ws/services`.
                
                **Auth:** `Sec-WebSocket-Protocol: bearer, <token>` \
                (short-lived ticket obtained from `GET /api/ws-token`)
                """
)
@Authenticated
@WebSocket(path = "/ws/services")
public class ServicesSocket {

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ServerService serverService;

    {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(ServiceStartedEvent.class, _ -> this.broadcastServicesUpdate());
        eventBus.subscribe(ServiceStartingEvent.class, _ -> this.broadcastServicesUpdate());
        eventBus.subscribe(ServiceStoppedEvent.class, _ -> this.broadcastServicesUpdate());
        eventBus.subscribe(ServiceStoppingEvent.class, _ -> this.broadcastServicesUpdate());
        eventBus.subscribe(CloudPlayerJoinEvent.class, _ -> this.broadcastServicesUpdate());
        eventBus.subscribe(CloudPlayerDisconnectEvent.class, _ -> this.broadcastServicesUpdate());
    }

    @OnOpen
    void onOpen() {
        broadcastServicesUpdate();
    }

    @Scheduled(every = "5s")
    void periodicServicesUpdate() {
        broadcastServicesUpdate();
    }

    void broadcastServicesUpdate() {
        if (openConnections.listAll().isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(
                    new WsEnvelope<>("services_update", serverService.findAll())
            );
            openConnections.listAll().forEach(c -> c.sendTextAndAwait(json));
        } catch (Exception e) {
            Log.error("Failed to broadcast services_update", e);
        }
    }

}
