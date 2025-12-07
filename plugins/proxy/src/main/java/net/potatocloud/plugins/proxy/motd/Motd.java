package net.potatocloud.plugins.proxy.motd;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
public class Motd {

    private final String firstLine;
    private final String secondLine;
    private final String version;

    public Motd(String firstLine, String secondLine) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.version = null;
    }
}
