package net.potatocloud.core.networking.packets.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAddPacket implements Packet {

    private Property<?> property;

    @Override
    public void write(PacketBuffer buf) {
        buf.writeProperty(property);
    }

    @Override
    public void read(PacketBuffer buf) {
        property = buf.readProperty();
    }

    @Override
    public int getId() {
        return PacketIds.PROPERTY_ADD;
    }
}
