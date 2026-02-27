package net.potatocloud.core.networking.packet.packets.player;

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
@NoArgsConstructor
@AllArgsConstructor
public class CloudPlayerAddPacket implements Packet {

    private String username;
    private UUID uniqueId;
    private String connectedProxyName;
    private String connectedServiceName;
    private Map<String, Property<?>> propertyMap;

    @Override
    public int getId() {
        return PacketIds.PLAYER_ADD;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(username);
        buf.writeString(uniqueId.toString());
        buf.writeString(connectedProxyName);
        buf.writeString(connectedServiceName);
        buf.writePropertyMap(propertyMap);
    }

    @Override
    public void read(PacketBuffer buf) {
        username = buf.readString();
        uniqueId = UUID.fromString(buf.readString());
        connectedProxyName = buf.readString();
        connectedServiceName = buf.readString();
        propertyMap = buf.readPropertyMap();
    }
}
