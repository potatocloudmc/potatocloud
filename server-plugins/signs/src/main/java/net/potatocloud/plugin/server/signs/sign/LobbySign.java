package net.potatocloud.plugin.server.signs.sign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.potatocloud.api.group.ServiceGroup;


@AllArgsConstructor
@Accessors(fluent = true)
@Data
public class LobbySign {

    private ServiceGroup serviceGroup;
    private SignLocation signLocation;
}
