package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.util.UUID;

public final class CloudPlayerSerializer implements TypeSerializer<CloudPlayer> {

    @Override
    public void write(PacketBuffer buffer, CloudPlayer player) {
        buffer.writeString(player.username());
        buffer.write(player.uniqueId(), UUID.class);
        buffer.writeString(player.proxy().name());
        buffer.writeString(player.service().map(Service::name).orElse(null));
        buffer.write(player.properties(), CollectionSerializers.propertyMap());
    }

    @Override
    public CloudPlayer read(PacketBuffer buffer) {
        return new CloudPlayerImpl(
                buffer.readString(),
                buffer.read(UUID.class),
                buffer.readString(),
                buffer.readString(),
                buffer.read(CollectionSerializers.propertyMap())
        );
    }
}
