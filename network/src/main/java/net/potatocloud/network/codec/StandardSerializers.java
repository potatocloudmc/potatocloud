package net.potatocloud.network.codec;

import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.network.codec.serializers.ObjectSerializer;
import net.potatocloud.network.codec.serializers.*;

import java.time.Instant;
import java.util.UUID;

public final class StandardSerializers {

    private StandardSerializers() {
    }

    public static void registerAll() {
        SerializerRegistry.register(String.class, new StringSerializer());
        SerializerRegistry.register(UUID.class, new UUIDSerializer());
        SerializerRegistry.register(Instant.class, new InstantSerializer());
        SerializerRegistry.register(Group.class, new GroupSerializer());
        SerializerRegistry.register(Service.class, new ServiceSerializer());
        SerializerRegistry.register(CloudPlayer.class, new CloudPlayerSerializer());
        SerializerRegistry.register(ClusterNode.class, new ClusterNodeSerializer());
        SerializerRegistry.register(Platform.class, new PlatformSerializer());
        SerializerRegistry.register(PlatformVersion.class, new PlatformVersionSerializer());
        SerializerRegistry.register(PropertyKey.class, new PropertyKeySerializer());
        SerializerRegistry.register(Object.class, new ObjectSerializer());
    }
}
