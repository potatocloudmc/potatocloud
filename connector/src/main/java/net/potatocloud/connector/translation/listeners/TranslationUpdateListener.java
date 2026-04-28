package net.potatocloud.connector.translation.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.api.translation.TranslationManager;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.translation.TranslationUpdatePacket;

@RequiredArgsConstructor
public class TranslationUpdateListener implements PacketListener<TranslationUpdatePacket> {

    private final TranslationManager translationManager;

    @Override
    public void onPacket(NetworkConnection connection, TranslationUpdatePacket packet) {
        final Translation translation = translationManager.getTranslation(packet.getKey());
        if (translation == null) {
            return;
        }
        translation.setValue(packet.getValue());
    }
}
