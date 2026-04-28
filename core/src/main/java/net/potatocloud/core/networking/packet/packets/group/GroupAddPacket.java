package net.potatocloud.core.networking.packet.packets.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.netty.PacketBuffer;
import net.potatocloud.core.networking.packet.Packet;
import net.potatocloud.core.networking.packet.PacketIds;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAddPacket implements Packet {

    private String name;
    private String platformName;
    private String platformVersionName;
    private String javaCommand;
    private List<String> customJvmFlags;
    private int maxPlayers;
    private int maxMemory;
    private int minOnlineCount;
    private int maxOnlineCount;
    private boolean isStatic;
    private boolean primary;
    private int startPriority;
    private int startPercentage;
    private List<String> serviceTemplates;
    private Map<String, Property<?>> propertyMap;

    @Override
    public int getId() {
        return PacketIds.GROUP_ADD;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeString(platformName);
        buf.writeString(platformVersionName);
        buf.writeString(javaCommand);
        buf.writeStringList(customJvmFlags);
        buf.writeInt(maxPlayers);
        buf.writeInt(maxMemory);
        buf.writeInt(minOnlineCount);
        buf.writeInt(maxOnlineCount);
        buf.writeBoolean(isStatic);
        buf.writeBoolean(primary);
        buf.writeInt(startPriority);
        buf.writeInt(startPercentage);
        buf.writeStringList(serviceTemplates);
        buf.writePropertyMap(propertyMap);
    }

    @Override
    public void read(PacketBuffer buf) {
        name = buf.readString();
        platformName = buf.readString();
        platformVersionName = buf.readString();
        javaCommand = buf.readString();
        customJvmFlags = buf.readStringList();
        maxPlayers = buf.readInt();
        maxMemory = buf.readInt();
        minOnlineCount = buf.readInt();
        maxOnlineCount = buf.readInt();
        isStatic = buf.readBoolean();
        primary = buf.readBoolean();
        startPriority = buf.readInt();
        startPercentage = buf.readInt();
        serviceTemplates = buf.readStringList();
        propertyMap = buf.readPropertyMap();
    }
}
