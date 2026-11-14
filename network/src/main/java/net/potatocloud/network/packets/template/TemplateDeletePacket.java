package net.potatocloud.network.packets.template;

import net.potatocloud.api.template.Template;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record TemplateDeletePacket(Template template) implements Packet {

    public static final Codec<TemplateDeletePacket> CODEC = new Codec<>() {

        @Override
        public void encode(TemplateDeletePacket packet, PacketBuffer buf) {
            buf.write(packet.template(), Template.class);
        }

        @Override
        public TemplateDeletePacket decode(PacketBuffer buf) {
            return new TemplateDeletePacket(buf.read(Template.class));
        }
    };
}
