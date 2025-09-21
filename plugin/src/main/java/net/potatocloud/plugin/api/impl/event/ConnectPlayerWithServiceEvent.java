package net.potatocloud.plugin.api.impl.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.potatocloud.api.event.Event;

@Data
@AllArgsConstructor
public class ConnectPlayerWithServiceEvent implements Event {

    private String playerUsername;
    private String serviceName;

}
