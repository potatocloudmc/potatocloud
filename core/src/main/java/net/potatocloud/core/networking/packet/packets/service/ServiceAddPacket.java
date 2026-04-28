package net.potatocloud.core.networking.packet.packets.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.netty.PacketBuffer;
import net.potatocloud.core.networking.packet.Packet;
import net.potatocloud.core.networking.packet.PacketIds;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAddPacket implements Packet {

    private String name;
    private int serviceId;
    private UUID serviceUuid;
    private int port;
    private long startTimestamp;
    private String groupName;
    private Map<String, Property<?>> propertyMap;
    private String status;
    private int maxPlayers;
    private String requestId;

    @Override
    public int getId() {
        return PacketIds.SERVICE_ADD;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeInt(serviceId);
        buf.writeUUID(serviceUuid);
        buf.writeInt(port);
        buf.writeLong(startTimestamp);
        buf.writeString(groupName);
        buf.writePropertyMap(propertyMap);
        buf.writeString(status);
        buf.writeInt(maxPlayers);
        buf.writeString(requestId);
    }

    @Override
    public void read(PacketBuffer buf) {
        name = buf.readString();
        serviceId = buf.readInt();
        serviceUuid = buf.readUUID();
        port = buf.readInt();
        startTimestamp = buf.readLong();
        groupName = buf.readString();
        propertyMap = buf.readPropertyMap();
        status = buf.readString();
        maxPlayers = buf.readInt();
        requestId = buf.readString();
    }
}
