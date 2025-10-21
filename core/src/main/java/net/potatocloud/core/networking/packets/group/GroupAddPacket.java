package net.potatocloud.core.networking.packets.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAddPacket implements Packet {

    private String name;
    private String platformName;
    private String platformVersionName;
    private int minOnlineCount;
    private int maxOnlineCount;
    private int maxPlayers;
    private int maxMemory;
    private boolean fallback;
    private boolean isStatic;
    private int startPriority;
    private int startPercentage;
    private String javaCommand;
    private List<String> customJvmFlags;
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
        buf.writeInt(minOnlineCount);
        buf.writeInt(maxOnlineCount);
        buf.writeInt(maxPlayers);
        buf.writeInt(maxMemory);
        buf.writeBoolean(fallback);
        buf.writeBoolean(isStatic);
        buf.writeInt(startPriority);
        buf.writeInt(startPercentage);
        buf.writeString(javaCommand);
        buf.writeStringList(customJvmFlags);
        buf.writePropertyMap(propertyMap);
    }

    @Override
    public void read(PacketBuffer buf) {
        name = buf.readString();
        platformName = buf.readString();
        platformVersionName = buf.readString();
        minOnlineCount = buf.readInt();
        maxOnlineCount = buf.readInt();
        maxPlayers = buf.readInt();
        maxMemory = buf.readInt();
        fallback = buf.readBoolean();
        isStatic = buf.readBoolean();
        startPriority = buf.readInt();
        startPercentage = buf.readInt();
        javaCommand = buf.readString();
        customJvmFlags = buf.readStringList();
        propertyMap = buf.readPropertyMap();
    }
}
