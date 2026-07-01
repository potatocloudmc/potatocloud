package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.time.Instant;

public final class ServiceSerializer implements TypeSerializer<Service> {

    @Override
    public void write(PacketBuffer buffer, Service service) {
        buffer.writeInt(service.id());
        buffer.writeString(service.host());
        buffer.writeInt(service.port());
        buffer.writeString(service.name());
        buffer.writeString(service.group() == null ? null : service.group().name());
        buffer.write(service.properties(), CollectionSerializers.propertyMap());
        buffer.write(service.startedAt(), Instant.class);
        buffer.writeString(service.state().name());
        buffer.writeInt(service.maxPlayers());
        buffer.writeInt(service.usedMemory());
    }

    @Override
    public Service read(PacketBuffer buffer) {
        return new ServiceImpl(
                buffer.readInt(),
                buffer.readString(),
                buffer.readInt(),
                buffer.readString(),
                buffer.readString(),
                buffer.read(CollectionSerializers.propertyMap()),
                buffer.read(Instant.class),
                ServiceState.valueOf(buffer.readString()),
                buffer.readInt(),
                buffer.readInt()
        );

    }
}
