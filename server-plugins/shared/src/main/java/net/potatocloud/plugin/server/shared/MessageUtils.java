package net.potatocloud.plugin.server.shared;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MessageUtils {

    private MessageUtils() {
    }

    public static Component format(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
