package net.potatocloud.network.netty;

import io.netty.buffer.ByteBuf;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.cluster.impl.SimpleClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.group.impl.GroupImpl;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformBase;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformImpl;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.player.impl.CloudPlayerImpl;
import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceState;
import net.potatocloud.api.service.impl.ServiceImpl;
import net.potatocloud.common.Pair;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class PacketBuffer {

    private final ByteBuf buf;

    public PacketBuffer(ByteBuf buf) {
        this.buf = buf;
    }

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
        if (list == null) {
            writeInt(-1);
            return;
        }
        writeInt(list.size());
        for (String item : list) {
            writeString(item);
        }
    }

    public List<String> readStringList() {
        final int size = readInt();
        if (size == -1) {
            return new ArrayList<>();
        }
        final List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readString());
        }
        return list;
    }

    public void writeStringSet(Set<String> set) {
        if (set == null) {
            writeInt(-1);
            return;
        }
        writeInt(set.size());
        for (String item : set) {
            writeString(item);
        }
    }

    public Set<String> readStringSet() {
        final int size = readInt();
        if (size == -1) {
            return new HashSet<>();
        }
        final Set<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(readString());
        }
        return set;
    }

    public void writeObject(Object object) {
        switch (object) {
            case null -> buf.writeByte(0);
            case String string -> {
                buf.writeByte(1);
                writeString(string);
            }
            case Integer integer -> {
                buf.writeByte(2);
                writeInt(integer);
            }
            case Boolean bool -> {
                buf.writeByte(3);
                writeBoolean(bool);
            }
            case Long l -> {
                buf.writeByte(4);
                writeLong(l);
            }
            case Float f -> {
                buf.writeByte(5);
                writeFloat(f);
            }
            case Double d -> {
                buf.writeByte(6);
                writeDouble(d);
            }
            default -> throw new IllegalArgumentException("Unsupported object: " + object.getClass());
        }
    }

    public Object readObject() {
        final byte type = buf.readByte();
        return switch (type) {
            case 0 -> null;
            case 1 -> readString();
            case 2 -> readInt();
            case 3 -> readBoolean();
            case 4 -> readLong();
            case 5 -> readFloat();
            case 6 -> readDouble();
            default -> throw new IllegalArgumentException("Unknown object id: " + type);
        };
    }

    public void writeProperty(PropertyKey<?> key, Object value) {
        writeString(key.name());
        writeObject(key.defaultValue());
        writeObject(value);
    }

    public Pair<PropertyKey<?>, Object> readProperty() {
        final String name = readString();
        final Object defaultValue = readObject();
        final Object value = readObject();

        return Pair.of(new PropertyKey<>(name, defaultValue), value);
    }

    public void writePropertyMap(Map<PropertyKey<?>, Object> propertyMap) {
        if (propertyMap == null) {
            writeInt(-1);
            return;
        }
        writeInt(propertyMap.size());
        for (Map.Entry<PropertyKey<?>, Object> entry : propertyMap.entrySet()) {
            writeProperty(entry.getKey(), entry.getValue());
        }
    }

    public Map<PropertyKey<?>, Object> readPropertyMap() {
        final int size = readInt();
        if (size == -1) {
            return new HashMap<>();
        }
        final Map<PropertyKey<?>, Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            final Pair<PropertyKey<?>, Object> pair = readProperty();
            map.put(pair.key(), pair.value());
        }
        return map;
    }

    public void writeClusterNode(ClusterNode node) {
        writeString(node.name());
        writeString(node.host());
        writeInt(node.port());
        writeLong(node.startedAt().toEpochMilli());
    }

    public ClusterNode readClusterNode() {
        return new SimpleClusterNode(readString(), readString(), readInt(), Instant.ofEpochMilli(readLong()));
    }

    public void writeClusterNodeList(List<ClusterNode> nodes) {
        if (nodes == null) {
            writeInt(-1);
            return;
        }
        writeInt(nodes.size());
        for (ClusterNode node : nodes) {
            writeClusterNode(node);
        }
    }

    public List<ClusterNode> readClusterNodeList() {
        final int size = readInt();
        if (size == -1) {
            return new ArrayList<>();
        }
        final List<ClusterNode> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readClusterNode());
        }
        return list;
    }

    public void writeGroupList(List<Group> groups) {
        if (groups == null) {
            writeInt(-1);
            return;
        }
        writeInt(groups.size());
        for (Group group : groups) {
            writeGroup(group);
        }
    }

    public List<Group> readGroupList() {
        final int size = readInt();
        if (size == -1) {
            return new ArrayList<>();
        }
        final List<Group> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readGroup());
        }
        return list;
    }

    public void writeServiceList(List<Service> services) {
        if (services == null) {
            writeInt(-1);
            return;
        }
        writeInt(services.size());
        for (Service service : services) {
            writeService(service);
        }
    }

    public List<Service> readServiceList() {
        final int size = readInt();
        if (size == -1) {
            return new ArrayList<>();
        }
        final List<Service> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readService());
        }
        return list;
    }

    public void writeCloudPlayerList(List<CloudPlayer> players) {
        if (players == null) {
            writeInt(-1);
            return;
        }
        writeInt(players.size());
        for (CloudPlayer player : players) {
            writeCloudPlayer(player);
        }
    }

    public List<CloudPlayer> readCloudPlayerList() {
        final int size = readInt();
        if (size == -1) {
            return new ArrayList<>();
        }
        final List<CloudPlayer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readCloudPlayer());
        }
        return list;
    }

    public void writePlatformList(List<Platform> platforms) {
        if (platforms == null) {
            writeInt(-1);
            return;
        }
        writeInt(platforms.size());
        for (Platform platform : platforms) {
            writePlatform(platform);
        }
    }

    public List<Platform> readPlatformList() {
        final int size = readInt();
        if (size == -1) {
            return new ArrayList<>();
        }
        final List<Platform> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readPlatform());
        }
        return list;
    }

    public void writeUUID(UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public UUID readUUID() {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public void writeLong(long value) {
        buf.writeLong(value);
    }

    public long readLong() {
        return buf.readLong();
    }

    public void writeFloat(float value) {
        buf.writeFloat(value);
    }

    public void writeDouble(double value) {
        buf.writeDouble(value);
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public double readDouble() {
        return buf.readDouble();
    }

    public void writeGroup(Group group) {
        writeString(group.name());
        writeString(group.node().map(ClusterNode::name).orElse(null));
        writeString(group.platform().name());
        writeString(group.platformVersion().name());
        writeString(group.javaCommand());
        writeStringSet(group.customJvmFlags());
        writeInt(group.maxPlayers());
        writeInt(group.maxMemory());
        writeInt(group.minServices());
        writeInt(group.maxServices());
        writeBoolean(group.staticServices());
        writeBoolean(group.fallback());
        writeInt(group.startPriority());
        writeInt(group.startPercentage());
        writeStringSet(group.templates());
        writePropertyMap(group.properties());
    }

    public Group readGroup() {
        return new GroupImpl(
                readString(),
                readString(),
                readString(),
                readString(),
                readString(),
                readStringSet(),
                readInt(),
                readInt(),
                readInt(),
                readInt(),
                readBoolean(),
                readBoolean(),
                readInt(),
                readInt(),
                readStringSet(),
                readPropertyMap()
        );
    }

    public void writeService(Service service) {
        writeInt(service.id());
        writeString(service.host());
        writeInt(service.port());
        writeString(service.name());
        writeString(service.group() == null ? null : service.group().name());
        writePropertyMap(service.properties());
        writeLong(service.startedAt().toEpochMilli());
        writeString(service.state().name());
        writeInt(service.maxPlayers());
        writeInt(service.usedMemory());
    }

    public Service readService() {
        return new ServiceImpl(
                readInt(),
                readString(),
                readInt(),
                readString(),
                readString(),
                readPropertyMap(),
                Instant.ofEpochMilli(readLong()),
                ServiceState.valueOf(readString()),
                readInt(),
                readInt()
        );
    }

    public void writeCloudPlayer(CloudPlayer player) {
        writeString(player.username());
        writeUUID(player.uniqueId());
        writeString(player.proxy().name());
        writeString(player.service().map(Service::name).orElse(null));
        writePropertyMap(player.properties());
    }

    public CloudPlayer readCloudPlayer() {
        return new CloudPlayerImpl(
                readString(),
                readUUID(),
                readString(),
                readString(),
                readPropertyMap()
        );
    }

    public void writePlatform(Platform platform) {
        writeString(platform.name());
        writeString(platform.downloadUrl());
        writeBoolean(platform.custom());
        writeBoolean(platform.proxy());
        writeString(platform.base().id());
        writeString(platform.preCacheBuilder());
        writeString(platform.parser());
        writeString(platform.hashType());
        writeStringList(platform.prepareSteps());

        writeInt(platform.versions().size());
        for (PlatformVersion version : platform.versions()) {
            writeString(version.platform().name());
            writeString(version.name());
            writeBoolean(version.local());
            writeString(version.downloadUrl());
            writeString(version.fileHash());
            writeBoolean(version.legacy());
        }
    }

    public Platform readPlatform() {
        final String name = readString();
        final String downloadUrl = readString();
        final boolean custom = readBoolean();
        final boolean isProxy = readBoolean();
        final PlatformBase base = PlatformBase.fromId(readString());
        final String preCacheBuilder = readString();
        final String parser = readString();
        final String hashType = readString();
        final List<String> prepareSteps = readStringList();

        final PlatformImpl platform = new PlatformImpl(
                name, downloadUrl, custom, isProxy, base, preCacheBuilder, parser, hashType, prepareSteps);

        final int versionCount = readInt();
        for (int i = 0; i < versionCount; i++) {
            final String platformName = readString();
            final String versionName = readString();
            final boolean local = readBoolean();
            final String versionDownloadUrl = readString();
            final String fileHash = readString();
            final boolean legacy = readBoolean();

            final PlatformVersion version = new PlatformVersionImpl(
                    platformName, versionName, local, versionDownloadUrl, fileHash, legacy);
            platform.versions().add(version);
        }

        return platform;
    }
}
