package net.potatocloud.core.networking.netty;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformImpl;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.api.property.Property;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PacketBuffer {

    private final ByteBuf buf;

    public void writeString(String string) {
        if (string == null) {
            buf.writeInt(-1);
            return;
        }
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public String readString() {
        final int length = buf.readInt();
        if (length == -1) {
            return null;
        }
        final byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void writeInt(int value) {
        buf.writeInt(value);
    }

    public int readInt() {
        return buf.readInt();
    }

    public void writeBoolean(boolean bool) {
        buf.writeBoolean(bool);
    }

    public boolean readBoolean() {
        return buf.readBoolean();
    }

    public void writeStringList(List<String> list) {
        writeInt(list.size());
        for (String item : list) {
            writeString(item);
        }
    }

    public List<String> readStringList() {
        final int size = readInt();
        final List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readString());
        }
        return list;
    }

    public void writeObject(Object object) {
        if (object instanceof String string) {
            buf.writeByte(1);
            writeString(string);
        } else if (object instanceof Integer integer) {
            buf.writeByte(2);
            writeInt(integer);
        } else if (object instanceof Boolean bool) {
            buf.writeByte(3);
            writeBoolean(bool);
        } else if (object instanceof Long l) {
            buf.writeByte(4);
            writeLong(l);
        } else {
            throw new IllegalArgumentException("Unsupported object: " + object.getClass());
        }
    }


    public Object readObject() {
        final byte type = buf.readByte();
        return switch (type) {
            case 1 -> readString();
            case 2 -> readInt();
            case 3 -> readBoolean();
            case 4 -> readLong();
            default -> throw new IllegalArgumentException("Unknown object id: " + type);
        };
    }

    public void writeProperty(Property property) {
        writeString(property.getName());
        writeObject(property.getDefaultValue());
        writeObject(property.getValue());
    }

    public Property readProperty() {
        final String name = readString();
        final Object defaultValue = readObject();
        final Object value = readObject();

        return Property.of(name, defaultValue, value);
    }

    public void writePropertySet(Set<Property> properties) {
        buf.writeInt(properties.size());
        for (Property property : properties) {
            writeProperty(property);
        }
    }

    public Set<Property> readPropertySet() {
        final int size = readInt();
        final Set<Property> properties = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            properties.add(readProperty());
        }
        return properties;
    }

    public void writeLong(long value) {
        buf.writeLong(value);
    }

    public long readLong() {
        return buf.readLong();
    }

    public void writePlatform(Platform platform) {
        writeString(platform.getName());
        writeString(platform.getDownloadUrl());
        writeBoolean(platform.isCustom());
        writeBoolean(platform.isProxy());
        writeString(platform.getBase());

        writeInt(platform.getVersions().size());
        for (PlatformVersion version : platform.getVersions()) {
            writeString(version.getPlatformName());
            writeString(version.getName());
            writeString(version.getDownloadUrl());
            writeString(version.getParser());
            writeString(version.getHashType());
            writeString(version.getFileHash());
            writeBoolean(version.isLegacy());
        }

        writeStringList(platform.getPrepareSteps());
    }

    public Platform readPlatform() {
        final String name = readString();
        final String downloadUrl = readString();
        final boolean custom = readBoolean();
        final boolean isProxy = readBoolean();
        final String base = readString();

        final PlatformImpl platform = new PlatformImpl(name, downloadUrl, custom, isProxy, base);

        final int versionCount = readInt();
        for (int i = 0; i < versionCount; i++) {
            final String platformName = readString();
            final String versionName = readString();
            final String versionDownloadUrl = readString();
            final String parser = readString();
            final String hashType = readString();
            final String fileHash = readString();
            final boolean legacy = readBoolean();


            final PlatformVersion version = new PlatformVersionImpl(
                    platformName, versionName, versionDownloadUrl, parser, hashType, fileHash, legacy);
            platform.getVersions().add(version);
        }

        platform.getPrepareSteps().addAll(readStringList());
        return platform;
    }

}
