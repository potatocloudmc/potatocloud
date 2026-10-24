package net.potatocloud.network.packets.template;

import net.potatocloud.api.template.Template;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record TemplateCreatePacket(Template template) implements Packet {

    public static final Codec<TemplateCreatePacket> CODEC = new Codec<>() {

        @Override
        public void encode(TemplateCreatePacket packet, PacketBuffer buf) {
            buf.write(packet.template(), Template.class);
        }

        @Override
        public TemplateCreatePacket decode(PacketBuffer buf) {
            return new TemplateCreatePacket(buf.read(Template.class));
        }
    };
}
