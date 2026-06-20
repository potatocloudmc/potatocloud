package net.potatocloud.node.screen.impl;

import net.potatocloud.node.screen.AbstractScreen;

public class SimpleScreen extends AbstractScreen {

    private final String name;

    public SimpleScreen(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void open() {}

    @Override
    public void close() {}
}
