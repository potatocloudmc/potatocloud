package net.potatocloud.node.console;

import net.potatocloud.node.Node;
import org.jline.jansi.Ansi;

public enum ConsoleColor {

    DARK_GRAY('8', 240),
    GRAY('7', 188),
    RED('c', 9),
    BLUE('9', 63),
    YELLOW('e', 220),
    GREEN('a', 42),
    WHITE('f', 15);

    private final char code;
    private final String ansiColor;

    ConsoleColor(char code, int ansi256Code) {
        this.code = code;
        this.ansiColor = Ansi.ansi().reset().fg(ansi256Code).toString();
    }

    public static String format(String text) {
        for (ConsoleColor color : values()) {
            if (color.code == 'a') {
                text = text.replace("&a", primaryColor());
            } else {
                text = text.replace("&" + color.code, color.ansiColor);
            }
        }
        return text + Ansi.ansi().reset();
    }

    private static String primaryColor() {
        return Ansi.ansi().reset().fg(Node.getInstance().getConfig().getPrimaryColorCode()).toString();
    }
}


