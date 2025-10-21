package net.potatocloud.core.networking.packets.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAddPacket implements Packet {

    private String name;
    private int serviceId;
    private int port;
    private long startTimestamp;
    private String groupName;
    private Map<String, Property<?>> propertyMap;
    private String status;
    private int maxPlayers;

    @Override
    public int getId() {
        return PacketIds.SERVICE_ADD;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeInt(serviceId);
        buf.writeInt(port);
        buf.writeLong(startTimestamp);
        buf.writeString(groupName);
        buf.writePropertyMap(propertyMap);
        buf.writeString(status);
        buf.writeInt(maxPlayers);
    }

    @Override
    public void read(PacketBuffer buf) {
        name = buf.readString();
        serviceId = buf.readInt();
        port = buf.readInt();
        startTimestamp = buf.readLong();
        groupName = buf.readString();
        propertyMap = buf.readPropertyMap();
        status = buf.readString();
        maxPlayers = buf.readInt();
    }
}
