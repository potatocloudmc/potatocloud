package net.potatocloud.node.console;

public final class ConsoleBanner {

    private ConsoleBanner() {
    }

    private static final String BANNER_TEXT = """
                             __        __             __                __
                ____  ____  / /_____ _/ /_____  _____/ /___  __  ______/ /
               / __ \\/ __ \\/ __/ __ \\/ __/ __ \\/ ___/ / __ \\/ / / / __  /\s
              / /_/ / /_/ / /_/ /_/ / /_/ /_/ / /__/ / /_/ / /_/ / /_/ / \s
             / ____/\\____/\\__/\\____/\\__/\\____/\\___/_/\\____/\\____/\\____/  \s
            /_/                                                          \s""";

    public static void display(Console console) {
        console.println(" ");
        console.println(BANNER_TEXT);
        console.println(" ");
    }
}
