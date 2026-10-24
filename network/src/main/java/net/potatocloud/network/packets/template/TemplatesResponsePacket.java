package net.potatocloud.network.packets.template;

import net.potatocloud.api.template.Template;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.List;

public record TemplatesResponsePacket(List<Template> templates) implements ResponsePacket {

    public static final Codec<TemplatesResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(TemplatesResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.templates(), CollectionSerializers.list(Template.class));
        }

        @Override
        public TemplatesResponsePacket decode(PacketBuffer buf) {
            return new TemplatesResponsePacket(buf.read(CollectionSerializers.list(Template.class)));
        }
    };
}
