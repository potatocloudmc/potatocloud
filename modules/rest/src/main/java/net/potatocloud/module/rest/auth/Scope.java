package net.potatocloud.module.rest.auth;

import io.javalin.security.RouteRole;

public enum Scope implements RouteRole {

    GROUP_READ("group.read"),
    GROUP_CREATE("group.create"),
    GROUP_UPDATE("group.update"),
    GROUP_DELETE("group.delete"),

    SERVICE_READ("service.read"),
    SERVICE_START("service.start"),
    SERVICE_STOP("service.stop"),
    SERVICE_COMMAND("service.command"),
    SERVICE_LOGS("service.logs"),

    PLATFORM_READ("platform.read"),
    PLATFORM_CREATE("platform.create"),
    PLATFORM_UPDATE("platform.update"),
    PLATFORM_DELETE("platform.delete"),

    PLAYER_READ("player.read"),
    PLAYER_CONNECT("player.connect"),
    PLAYER_KICK("player.kick"),

    NODE_READ("node.read"),

    TEMPLATE_READ("template.read"),
    TEMPLATE_CREATE("template.create"),
    TEMPLATE_DELETE("template.delete"),
    TEMPLATE_COPY("template.copy"),

    PROPERTY_READ("property.read"),
    PROPERTY_WRITE("property.write");

    private final String id;

    Scope(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
