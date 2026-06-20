package net.potatocloud.node.screen.impl;

import net.potatocloud.api.service.Service;
import net.potatocloud.node.console.Console;

public class LocalServiceScreen extends ServiceScreen {

    public LocalServiceScreen(Service service, Console console) {
        super(service, console);
    }

    @Override
    public void open() {
        logs().forEach(console::println);

        console.setPrompt(buildPrompt());
    }

    @Override
    public void close() {}
}
