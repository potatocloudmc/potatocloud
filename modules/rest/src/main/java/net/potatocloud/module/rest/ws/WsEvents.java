package net.potatocloud.module.rest.ws;

import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.EventHandler;
import net.potatocloud.api.event.events.cluster.ClusterNodeJoinedEvent;
import net.potatocloud.api.event.events.cluster.ClusterNodeLeftEvent;
import net.potatocloud.api.event.events.group.GroupCreatedEvent;
import net.potatocloud.api.event.events.group.GroupDeletedEvent;
import net.potatocloud.api.event.events.group.GroupUpdatedEvent;
import net.potatocloud.api.event.events.platform.PlatformCreatedEvent;
import net.potatocloud.api.event.events.platform.PlatformDeletedEvent;
import net.potatocloud.api.event.events.platform.PlatformUpdatedEvent;
import net.potatocloud.api.event.events.player.CloudPlayerDisconnectEvent;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.api.event.events.property.PropertyChangedEvent;
import net.potatocloud.api.event.events.service.ServiceStartedEvent;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;

public final class WsEvents {

    private final EventBus eventBus;
    private final WsServer server;

    private final EventHandler<GroupCreatedEvent> groupCreated = event -> send("group_created", event);
    private final EventHandler<GroupUpdatedEvent> groupUpdated = event -> send("group_updated", event);
    private final EventHandler<GroupDeletedEvent> groupDeleted = event -> send("group_deleted", event);

    private final EventHandler<ServiceStartingEvent> serviceStarting = event -> send("service_starting", event);
    private final EventHandler<ServiceStartedEvent> serviceStarted = event -> send("service_started", event);
    private final EventHandler<ServiceStoppingEvent> serviceStopping = event -> send("service_stopping", event);
    private final EventHandler<ServiceStoppedEvent> serviceStopped = event -> send("service_stopped", event);

    private final EventHandler<PlatformCreatedEvent> platformCreated = event -> send("platform_created", event);
    private final EventHandler<PlatformUpdatedEvent> platformUpdated = event -> send("platform_updated", event);
    private final EventHandler<PlatformDeletedEvent> platformDeleted = event -> send("platform_deleted", event);

    private final EventHandler<CloudPlayerJoinEvent> playerJoin = event -> send("player_join", event);
    private final EventHandler<CloudPlayerDisconnectEvent> playerDisconnect = event -> send("player_disconnect", event);

    private final EventHandler<ClusterNodeJoinedEvent> nodeJoined = event -> send("node_joined", event);
    private final EventHandler<ClusterNodeLeftEvent> nodeLeft = event -> send("node_left", event);

    private final EventHandler<PropertyChangedEvent> propertyChanged = event -> send("property_changed", event);

    public WsEvents(EventBus eventBus, WsServer server) {
        this.eventBus = eventBus;
        this.server = server;
    }

    public void start() {
        eventBus.subscribe(GroupCreatedEvent.class, groupCreated);
        eventBus.subscribe(GroupUpdatedEvent.class, groupUpdated);
        eventBus.subscribe(GroupDeletedEvent.class, groupDeleted);

        eventBus.subscribe(ServiceStartingEvent.class, serviceStarting);
        eventBus.subscribe(ServiceStartedEvent.class, serviceStarted);
        eventBus.subscribe(ServiceStoppingEvent.class, serviceStopping);
        eventBus.subscribe(ServiceStoppedEvent.class, serviceStopped);

        eventBus.subscribe(PlatformCreatedEvent.class, platformCreated);
        eventBus.subscribe(PlatformUpdatedEvent.class, platformUpdated);
        eventBus.subscribe(PlatformDeletedEvent.class, platformDeleted);

        eventBus.subscribe(CloudPlayerJoinEvent.class, playerJoin);
        eventBus.subscribe(CloudPlayerDisconnectEvent.class, playerDisconnect);

        eventBus.subscribe(ClusterNodeJoinedEvent.class, nodeJoined);
        eventBus.subscribe(ClusterNodeLeftEvent.class, nodeLeft);

        eventBus.subscribe(PropertyChangedEvent.class, propertyChanged);
    }

    public void stop() {
        eventBus.unsubscribe(GroupCreatedEvent.class, groupCreated);
        eventBus.unsubscribe(GroupUpdatedEvent.class, groupUpdated);
        eventBus.unsubscribe(GroupDeletedEvent.class, groupDeleted);

        eventBus.unsubscribe(ServiceStartingEvent.class, serviceStarting);
        eventBus.unsubscribe(ServiceStartedEvent.class, serviceStarted);
        eventBus.unsubscribe(ServiceStoppingEvent.class, serviceStopping);
        eventBus.unsubscribe(ServiceStoppedEvent.class, serviceStopped);

        eventBus.unsubscribe(PlatformCreatedEvent.class, platformCreated);
        eventBus.unsubscribe(PlatformUpdatedEvent.class, platformUpdated);
        eventBus.unsubscribe(PlatformDeletedEvent.class, platformDeleted);

        eventBus.unsubscribe(CloudPlayerJoinEvent.class, playerJoin);
        eventBus.unsubscribe(CloudPlayerDisconnectEvent.class, playerDisconnect);

        eventBus.unsubscribe(ClusterNodeJoinedEvent.class, nodeJoined);
        eventBus.unsubscribe(ClusterNodeLeftEvent.class, nodeLeft);

        eventBus.unsubscribe(PropertyChangedEvent.class, propertyChanged);
    }

    private void send(String event, Object data) {
        server.sendEvent(event, data);
    }
}
