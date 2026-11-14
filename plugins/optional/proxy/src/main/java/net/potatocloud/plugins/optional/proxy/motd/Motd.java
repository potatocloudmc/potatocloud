package net.potatocloud.plugins.optional.proxy.motd;

public record Motd(String firstLine, String secondLine, String version) {

    public Motd(String firstLine, String secondLine) {
        this(firstLine, secondLine, null);
    }
}
