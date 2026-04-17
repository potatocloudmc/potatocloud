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
public class GroupUpdatePacket implements Packet {

    private String name;
    private List<String> customJvmFlags;
    private int maxPlayers;
    private int maxMemory;
    private int minOnlineCount;
    private int maxOnlineCount;
    private boolean fallback;
    private int startPriority;
    private int startPercentage;
    private List<String> serviceTemplates;
    private Map<String, Property<?>> propertyMap;

    @Override
    public int getId() {
        return PacketIds.GROUP_UPDATE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeStringList(customJvmFlags);
        buf.writeInt(minOnlineCount);
        buf.writeInt(maxOnlineCount);
        buf.writeInt(maxPlayers);
        buf.writeInt(maxMemory);
        buf.writeBoolean(fallback);
        buf.writeInt(startPriority);
        buf.writeInt(startPercentage);
        buf.writeStringList(serviceTemplates);
        buf.writePropertyMap(propertyMap);
    }

    @Override
    public void read(PacketBuffer buf) {
        name = buf.readString();
        customJvmFlags = buf.readStringList();
        minOnlineCount = buf.readInt();
        maxOnlineCount = buf.readInt();
        maxPlayers = buf.readInt();
        maxMemory = buf.readInt();
        fallback = buf.readBoolean();
        startPriority = buf.readInt();
        startPercentage = buf.readInt();
        serviceTemplates = buf.readStringList();
        propertyMap = buf.readPropertyMap();
    }
}
