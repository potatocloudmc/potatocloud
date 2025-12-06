package net.potatocloud.core.networking.packets.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.Packet;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.netty.PacketBuffer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyUpdatePacket implements Packet {

    private String name;
    private Object value;

    @Override
    public int getId() {
        return PacketIds.PROPERTY_UPDATE;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeObject(value);
    }

    @Override
    public void read(PacketBuffer buf) {
        name = buf.readString();
        value = buf.readObject();
    }
}
