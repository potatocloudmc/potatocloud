package net.potatocloud.eventbus;

import net.potatocloud.api.event.Event;
import net.potatocloud.network.packet.packets.event.EventPacket;
import tools.jackson.databind.json.JsonMapper;

public final class JsonEventCodec {

    private JsonEventCodec() {
    }

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public static EventPacket encode(Event event) {
        return new EventPacket(event.getClass().getName(), MAPPER.writeValueAsString(event));
    }

    public static Event decode(EventPacket packet) {
        try {
            final Class<?> clazz = Class.forName(packet.eventClass());

            if (!Event.class.isAssignableFrom(clazz)) {
                throw new RuntimeException("Class " + packet.eventClass() + " does not implement Event");
            }

            return (Event) MAPPER.readValue(packet.eventJson(), clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event: " + packet.eventClass(), e);
        }
    }
}