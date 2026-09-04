package net.potatocloud.node.screen;

public record Screen(String name, ScreenType type, String prompt, String remoteNode) {

    public static final String NODE_SCREEN_NAME = "Node";

    public static Screen node(String prompt) {
        return new Screen(NODE_SCREEN_NAME, ScreenType.NODE, prompt, null);
    }

    public static Screen service(String name) {
        return new Screen(name, ScreenType.SERVICE, "[" + name + " | leave] ", null);
    }

    public static Screen remoteService(String name, String nodeName) {
        return new Screen(name, ScreenType.SERVICE, "[" + name + " | leave] ", nodeName);
    }

    public static Screen setup(String name) {
        return new Screen(name, ScreenType.SETUP, "> ", null);
    }

    public boolean isRemote() {
        return remoteNode != null;
    }
}
