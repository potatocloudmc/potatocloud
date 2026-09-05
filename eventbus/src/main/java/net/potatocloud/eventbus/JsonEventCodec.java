package net.potatocloud.eventbus;

import net.potatocloud.api.event.Event;
import net.potatocloud.network.packets.event.EventPacket;
import tools.jackson.databind.json.JsonMapper;

public final class JsonEventCodec {

    private JsonEventCodec() {
    }

    private static final String EVENT_PACKAGE = "net.potatocloud.api.event.events.";
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public static EventPacket encode(Event event) {
        final String eventClass = event.getClass().getName();
        if (!eventClass.startsWith(EVENT_PACKAGE)) {
            throw new IllegalArgumentException("Event class is not allowed on the network: " + eventClass);
        }

        return new EventPacket(eventClass, MAPPER.writeValueAsString(event));
    }

    public static Event decode(EventPacket packet) {
        if (packet.eventClass() == null || !packet.eventClass().startsWith(EVENT_PACKAGE)) {
            throw new IllegalArgumentException("Event class is not allowed on the network: " + packet.eventClass());
        }

        try {
            final Class<?> eventClass = Class.forName(packet.eventClass(), false, Event.class.getClassLoader());
            if (!Event.class.isAssignableFrom(eventClass)) {
                throw new IllegalArgumentException("Class does not implement Event: " + packet.eventClass());
            }

            return (Event) MAPPER.readValue(packet.eventJson(), eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event: " + packet.eventClass(), e);
        }
    }
}