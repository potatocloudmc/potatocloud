package net.potatocloud.node.screen.impl;

import net.potatocloud.api.service.Service;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.AbstractScreen;

public abstract class ServiceScreen extends AbstractScreen {

    protected final Service service;
    protected final Console console;

    public ServiceScreen(Service service, Console console) {
        this.service = service;
        this.console = console;
    }

    @Override
    public String name() {
        return service.name();
    }

    protected String buildPrompt() {
        return "[" + service.name() + " | leave] ";
    }
}
