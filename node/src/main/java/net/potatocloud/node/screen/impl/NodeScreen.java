package net.potatocloud.node.screen.impl;

import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.AbstractScreen;

public class NodeScreen extends AbstractScreen {

    public static final String NODE_SCREEN_NAME = "Node";

    private final Console console;

    public NodeScreen(Console console) {
        this.console = console;
    }

    @Override
    public void open() {
        logs().stream()
                .filter(log -> !log.toLowerCase().contains("service screen"))
                .forEach(console::println);

        console.setPrompt(console.defaultPrompt());
    }

    @Override
    public void close() {}

    @Override
    public String name() {
        return NODE_SCREEN_NAME;
    }
}
