package net.potatocloud.core.networking.packet.packets.translation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.potatocloud.core.networking.netty.PacketBuffer;
import net.potatocloud.core.networking.packet.Packet;
import net.potatocloud.core.networking.packet.PacketIds;

@NoArgsConstructor
@AllArgsConstructor
public class RequestTranslationPacket implements Packet {

    @Getter
    private String translationGroup;

    @Override
    public int getId() {
        return PacketIds.REQUEST_TRANSLATION;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(translationGroup);
    }

    @Override
    public void read(PacketBuffer buf) {
        translationGroup = buf.readString();
    }
}
