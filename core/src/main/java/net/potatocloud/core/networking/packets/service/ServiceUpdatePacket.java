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
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdatePacket implements Packet {

    private String serviceName;
    private String status;
    private int maxPlayers;
    private Map<String, Property<?>> propertyMap;

    @Override
    public int getId() {
        return PacketIds.SERVICE_UPDATE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(serviceName);
        buf.writeString(status);
        buf.writeInt(maxPlayers);
        buf.writePropertyMap(propertyMap);
    }

    @Override
    public void read(PacketBuffer buf) {
        serviceName = buf.readString();
        status = buf.readString();
        maxPlayers = buf.readInt();
        propertyMap = buf.readPropertyMap();
    }
}
