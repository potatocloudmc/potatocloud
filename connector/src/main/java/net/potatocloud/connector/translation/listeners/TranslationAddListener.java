package net.potatocloud.connector.translation.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.connector.translation.TranslationImpl;
import net.potatocloud.connector.translation.TranslationManagerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.translation.TranslationAddPacket;

import java.util.List;

@RequiredArgsConstructor
public class TranslationAddListener implements PacketListener<TranslationAddPacket> {

    private final TranslationManagerImpl translationManager;

    @Override
    public void onPacket(NetworkConnection connection, TranslationAddPacket packet) {
        final Translation translation = new TranslationImpl(
                packet.getGroup(),
                packet.getLocale(),
                packet.getKey(),
                packet.getValue()
        );
        final List<Translation> translations = translationManager.getAllTranslations();
        if (!translations.contains(translation)) {
            IO.println(translation.getGroup());
            translationManager.addTranslation(translation);
        }
    }
}
