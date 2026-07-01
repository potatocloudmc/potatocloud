package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformBase;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformImpl;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.util.List;

public final class PlatformSerializer implements TypeSerializer<Platform> {

    @Override
    public void write(PacketBuffer buffer, Platform platform) {
        buffer.writeString(platform.name());
        buffer.writeString(platform.downloadUrl());
        buffer.writeBoolean(platform.custom());
        buffer.writeBoolean(platform.proxy());
        buffer.writeString(platform.base().id());
        buffer.writeString(platform.preCacheBuilder());
        buffer.writeString(platform.parser());
        buffer.writeString(platform.hashType());
        buffer.write(platform.prepareSteps(), CollectionSerializers.list(String.class));
        buffer.write(platform.versions(), CollectionSerializers.list(PlatformVersion.class));
    }

    @Override
    public Platform read(PacketBuffer buffer) {
        final PlatformImpl platform = new PlatformImpl(
                buffer.readString(),
                buffer.readString(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                PlatformBase.fromId(buffer.readString()),
                buffer.readString(),
                buffer.readString(),
                buffer.readString(),
                buffer.read(CollectionSerializers.list(String.class))
        );

        final List<PlatformVersion> versions = buffer.read(CollectionSerializers.list(PlatformVersion.class));
        platform.versions(versions);

        return platform;
    }
}
