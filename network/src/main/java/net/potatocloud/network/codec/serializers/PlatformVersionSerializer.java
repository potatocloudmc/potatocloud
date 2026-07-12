package net.potatocloud.network.codec.serializers;

import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

public final class PlatformVersionSerializer implements TypeSerializer<PlatformVersion> {

    @Override
    public void write(PacketBuffer buffer, PlatformVersion version) {
        buffer.writeString(version.platform().name());
        buffer.writeString(version.name());
        buffer.writeBoolean(version.local());
        buffer.writeString(version.downloadUrl());
        buffer.writeString(version.fileHash());
        buffer.writeBoolean(version.legacy());
    }

    @Override
    public PlatformVersion read(PacketBuffer buffer) {
        return new PlatformVersionImpl(
                buffer.readString(),
                buffer.readString(),
                buffer.readBoolean(),
                buffer.readString(),
                buffer.readString(),
                buffer.readBoolean()
        );
    }
}
