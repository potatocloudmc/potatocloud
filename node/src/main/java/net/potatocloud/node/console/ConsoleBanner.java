package net.potatocloud.node.console;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConsoleBanner {

    private static final String BANNER_TEXT = "                 __        __             __                __\n" +
            "    ____  ____  / /_____ _/ /_____  _____/ /___  __  ______/ /\n" +
            "   / __ \\/ __ \\/ __/ __ \\/ __/ __ \\/ ___/ / __ \\/ / / / __  / \n" +
            "  / /_/ / /_/ / /_/ /_/ / /_/ /_/ / /__/ / /_/ / /_/ / /_/ /  \n" +
            " / ____/\\____/\\__/\\____/\\__/\\____/\\___/_/\\____/\\____/\\____/   \n" +
            "/_/                                                           ";

    public void display(Console console) {
        console.println(" ");
        console.println(BANNER_TEXT);
        console.println(" ");
    }
}
