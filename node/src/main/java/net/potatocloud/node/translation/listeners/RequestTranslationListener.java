package net.potatocloud.node.translation.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.api.translation.TranslationManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.translation.RequestTranslationPacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationAddPacket;

@RequiredArgsConstructor
public class RequestTranslationListener implements PacketListener<RequestTranslationPacket> {

    private final TranslationManager translationManager;

    @Override
    public void onPacket(NetworkConnection connection, RequestTranslationPacket packet) {
        for (Translation translation : translationManager.getAllTranslations().stream().filter(translation -> translation.getGroup().equals(packet.getTranslationParent())).toList()) {
            connection.send(new TranslationAddPacket(
                    translation.getGroup(),
                    translation.getLocale(),
                    translation.getKey(),
                    translation.getValue()
            ));
        }
    }
}
