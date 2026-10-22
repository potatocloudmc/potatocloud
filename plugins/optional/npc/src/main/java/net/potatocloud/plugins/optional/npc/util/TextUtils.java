package net.potatocloud.plugins.optional.npc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextUtils {

    private TextUtils() {
    }

    public static Component component(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text.replace('&', '§'));
    }

    public static Component itemComponent(String text) {
        return component(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
