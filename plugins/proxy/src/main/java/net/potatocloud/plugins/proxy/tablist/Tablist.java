package net.potatocloud.plugins.proxy.tablist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class Tablist {

    private final String header;
    private final String footer;
}
